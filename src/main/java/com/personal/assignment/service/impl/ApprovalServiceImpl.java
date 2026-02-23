package com.personal.assignment.service.impl;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import com.personal.assignment.model.Approval;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import com.personal.assignment.service.ApprovalService;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;

    private final DocumentRepository documentRepository;

    private final HistoryRepository historyRepository;

    private final ConcurrentHashMap<Long, Mono<DocumentOpResult>> activeApprovalOps = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    public ApprovalServiceImpl(@Autowired ApprovalRepository approvalRepository,
                               @Autowired DocumentRepository documentRepository,
                               @Autowired HistoryRepository historyRepository) {
        this.approvalRepository = approvalRepository;
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional
    public Mono<DocumentOpResult> createApprovalEntry(Long documentId) {
        log.info("createApprovalEntry() >> creating approval entry for document {}", documentId);
        return documentRepository.findById(documentId)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(document -> approvalRepository.insertApproval(documentId, ZonedDateTime.now()))
            .thenReturn(new DocumentOpResult(documentId, OperationStatus.SUCCESS))
            .onErrorReturn(NotFoundException.class, new DocumentOpResult(documentId, OperationStatus.NOT_FOUND));
    }

    @Override
    public Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator) {
        log.info("approveBatch() >> beginning operation on {} documents", documentIds.size());

        AtomicInteger progress = new AtomicInteger();
        int size = documentIds.size();

        long startTime = System.currentTimeMillis();

        return Flux.fromIterable(documentIds)
            .flatMap(documentId -> approveDoc(documentId, initiator)
                .doFinally(ignored -> log.info("approveBatch() >> Approval progress {}/{}", progress.incrementAndGet(), size))
            )
            .doOnComplete(() -> log.info("approveBatch() >> finished approval of {} documents, completed in {} ms",
                documentIds.size(),
                System.currentTimeMillis() - startTime));
    }

    @Override
    public Mono<DocumentOpResult> approveDocumentById(Long documentId, String initiator) {
        log.info("approveDocumentById() >> beginning operation on document {}", documentId);
        return approveDoc(documentId, initiator);
    }

    @Override
    public Flux<Approval> getApprovals() {
        return approvalRepository.findAll();
    }

    @Transactional
    protected Mono<DocumentOpResult> approveDocument(Document doc, String initiator) {
        if (doc.getStatus() != DocumentStatus.SUBMITTED) {
            return Mono.error(
                new StatusChangeException("Can't approve document %s".formatted(doc.getId()),
                    doc.getId())
            );
        }
        return documentRepository.updateStatusById(doc.getId(), DocumentStatus.APPROVED)
            .subscribeOn(Schedulers.boundedElastic())
            .then(historyRepository.save(History.builder()
                    .initiator(initiator)
                    .documentId(doc.getId())
                    .documentAction(DocumentAction.APPROVE)
                    .actionDate(ZonedDateTime.now())
                    .build()
                )
            )
            .then(this.createApprovalEntry(doc.getId()))
            .thenReturn(new DocumentOpResult(doc.getId(), OperationStatus.SUCCESS));
    }

    @Transactional
    protected Mono<DocumentOpResult> approveDoc(Long documentId, String initiator) {
        if (activeApprovalOps.containsKey(documentId)) {
            return Mono.just(new DocumentOpResult(documentId, OperationStatus.CONFLICT));
        }

        return activeApprovalOps.computeIfAbsent(documentId, key ->
            documentRepository.findById(documentId)
                .flatMap(doc -> this.approveDocument(doc, initiator))
                .onErrorReturn(NotFoundException.class,
                    new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)
                )
                .onErrorReturn(StatusChangeException.class,
                    new DocumentOpResult(documentId, OperationStatus.CONFLICT)
                )
                .doFinally(signalType -> {
                    log.info("approveDocumentById() >> finished approval on document {}", documentId);
                    activeApprovalOps.remove(documentId);
                })
        );
    }
}
