package com.personal.assignment.service.impl;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import com.personal.assignment.model.Approval;
import com.personal.assignment.model.History;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import com.personal.assignment.service.ApprovalService;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;

    private final DocumentRepository documentRepository;

    private final HistoryRepository historyRepository;

    private final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    public ApprovalServiceImpl(@Autowired ApprovalRepository approvalRepository,
                               @Autowired DocumentRepository documentRepository,
                               @Autowired HistoryRepository historyRepository) {
        this.approvalRepository = approvalRepository;
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Mono<DocumentOpResult> createApprovalEntry(Long documentId) {
        log.info("createApprovalEntry() >> creating approval entry for document {}", documentId);
        return approvalRepository.insertApproval(documentId, ZonedDateTime.now())
            .thenReturn(new DocumentOpResult(documentId, OperationStatus.SUCCESS))
            .onErrorReturn(NotFoundException.class, new DocumentOpResult(documentId, OperationStatus.NOT_FOUND))
            .onErrorReturn(StatusChangeException.class, new DocumentOpResult(documentId, OperationStatus.CONFLICT));
    }

    @Override
    public Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator) {
        AtomicInteger progress = new AtomicInteger();
        int size = documentIds.size();

        long startTime = System.currentTimeMillis();

        return Flux.fromIterable(documentIds)
            .flatMap(documentId -> approveDocument(documentId, initiator))
            .doOnNext(ignored -> log.info("approveBatch() >> Approval progress {}/{}", progress.incrementAndGet(), size))
            .doOnComplete(() ->
                log.info("approveBatch() >> finished approval of {} documents, completed in {} ms",
                    documentIds.size(), System.currentTimeMillis() - startTime
            ));
    }

    @Override
    public Mono<DocumentOpResult> approveDocumentById(Long documentId, String initiator) {
        log.info("approveDocumentById() >> beginning operation on document {}", documentId);
        return approveDocument(documentId, initiator);
    }

    @Override
    public Flux<Approval> getApprovals() {
        return approvalRepository.findAll();
    }

    protected Mono<DocumentOpResult> approveDocument(Long documentId, String initiator) {
        return documentRepository.updateStatusById(documentId, DocumentStatus.APPROVED)
            .flatMap(rowsChanged -> rowsChanged < 1 ?
                Mono.error(new StatusChangeException("Could not change status of document with id %s"
                    .formatted(documentId), documentId)
                ) :
                Mono.just(rowsChanged)
            )
            .then(this.createApprovalEntry(documentId))
            .then(historyRepository.save(History.builder()
                    .initiator(initiator)
                    .documentId(documentId)
                    .documentAction(DocumentAction.APPROVE)
                    .actionDate(ZonedDateTime.now())
                    .build()
                )
            )
            .thenReturn(new DocumentOpResult(documentId, OperationStatus.SUCCESS))
            .onErrorReturn(NotFoundException.class,
                new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)
            )
            .onErrorReturn(StatusChangeException.class,
                new DocumentOpResult(documentId, OperationStatus.CONFLICT)
            )
            .onErrorReturn(Exception.class, new DocumentOpResult(documentId, OperationStatus.ERROR));
    }
}
