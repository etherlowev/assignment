package com.personal.assignment.service.impl;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.service.ApprovalService;
import com.personal.assignment.service.DocumentService;
import com.personal.assignment.service.HistoryService;
import com.personal.assignment.model.Document;
import java.time.ZonedDateTime;
import java.util.List;
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
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;

    private final HistoryService historyService;

    private final ApprovalService approvalService;

    private final ConcurrentHashMap<Long, Mono<DocumentOpResult>> activeSubmissionOps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Mono<DocumentOpResult>> activeApprovalOps = new ConcurrentHashMap<>();

    public DocumentServiceImpl(@Autowired DocumentRepository documentRepository,
                               @Autowired HistoryService historyService,
                               @Autowired ApprovalService approvalService) {
        this.documentRepository = documentRepository;
        this.historyService = historyService;
        this.approvalService = approvalService;
    }

    @Override
    public Mono<Document> createDocument(String author, String title) {
        return documentRepository.insertDocument(
            author,
            title,
            DocumentStatus.DRAFT,
            ZonedDateTime.now(),
            null
        );
    }

    @Override
    public Flux<Document> createDocumentBatch(String author, List<String> titles) {
        log.info("createDocumentBatch() >> Creating {} documents", titles.size());
        long startTime = System.currentTimeMillis();

        AtomicInteger counter = new AtomicInteger();
        int total = titles.size();

        return Flux.fromIterable(titles)
            .parallel()
            .flatMap(title -> createDocument(author, title)
                .doOnNext(doc -> log.info("createDocumentBatch() >> Batch creation progress {}/{}",
                    counter.incrementAndGet(), total))
            )
            .sequential(10)
            .doFinally(signalType -> log.info("createDocumentBatch() >> Finished batch creation in {} ms",
                System.currentTimeMillis() - startTime)
            );
    }

    @Override
    public Mono<DocumentOpResult> submitDocumentById(Long documentId, String initiator) {
        return activeSubmissionOps.containsKey(documentId) ?
            Mono.just(new DocumentOpResult(documentId, OperationStatus.CONFLICT)) :
            activeSubmissionOps.computeIfAbsent(documentId, key ->
                documentRepository.findById(documentId)
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(doc -> submitDocument(doc, initiator))
                    .onErrorResume(NotFoundException.class,
                        ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)))
                    .onErrorResume(StatusChangeException.class,
                        ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.CONFLICT)))
                    .onErrorResume(Throwable.class,
                        ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.ERROR)))
        );
    }

    @Override
    public Flux<DocumentOpResult> submitBatch(Set<Long> documentIds, String initiator) {
        log.info("submitBatch() >> Batch submitting {} documents, initiated by {}", documentIds.size(), initiator);

        AtomicInteger progress = new AtomicInteger();
        int size = documentIds.size();

        long startTime = System.currentTimeMillis();

        return Flux.fromIterable(documentIds)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(documentId -> this.submitDocumentById(documentId, initiator)
                .doOnNext(ignored -> log.info("submitBatch() >> Submission progress {}/{}", progress.incrementAndGet(), size)))
            .sequential(10)
            .doOnComplete(() -> log.info("submitBatch() >> Finished batch submission on {} objects, finished in {} ms",
                documentIds.size(),
                System.currentTimeMillis() - startTime));
    }

    @Override
    public Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator) {
        log.info("approveBatch() >> beginning operation on {} documents", documentIds.size());

        AtomicInteger progress = new AtomicInteger();
        int size = documentIds.size();

        long startTime = System.currentTimeMillis();

        return Flux.fromIterable(documentIds)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(documentId -> this.approveDocumentById(documentId, initiator)
                .doOnNext(ignored -> log.info("approveBatch() >> Approval progress {}/{}",
                    progress.incrementAndGet(), size))
            )
            .sequential(10)
            .doOnComplete(() -> log.info("approveBatch() >> finished approval of {} documents, completed in {} ms",
                documentIds.size(),
                System.currentTimeMillis() - startTime));
    }

    @Override
    public Mono<DocumentOpResult> approveDocumentById(Long documentId, String initiator) {
        log.info("approveDocumentById() >> beginning operation on document {}", documentId);

        return activeApprovalOps.containsKey(documentId) ?
            Mono.just(new DocumentOpResult(documentId, OperationStatus.CONFLICT))
            : activeApprovalOps.computeIfAbsent(documentId, key ->
                documentRepository.findById(documentId)
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(doc -> this.approveDocument(doc, initiator))
                    .onErrorReturn(NotFoundException.class,
                        new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)
                    )
                    .onErrorReturn(StatusChangeException.class,
                        new DocumentOpResult(documentId, OperationStatus.CONFLICT)
                    )
                    .onErrorReturn(Throwable.class,
                        new DocumentOpResult(documentId, OperationStatus.ERROR)
                    )
                    .doFinally(signalType -> {
                        log.info("approveDocumentById() >> finished approval on document {}", documentId);
                        activeApprovalOps.remove(documentId);
                    })
        );
    }

    @Override
    public Mono<DocumentWithHistory> getDocumentById(Long id) {
        return Mono.zip(
                documentRepository.findById(id),
                historyService.getHistory(id).collectList()
            )
            .map(tuple -> new DocumentWithHistory(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Flux<Document> getDocuments(DocumentFilteredPaging filteredPaging) {
        return documentRepository.getPage(filteredPaging.getCriteria(), filteredPaging.getPaging());
    }

    private Mono<DocumentOpResult> submitDocument(Document doc, String initiator) {
        if (doc.getStatus() != DocumentStatus.DRAFT) {
            return Mono.error(
                new StatusChangeException("Can't submit document %s".formatted(doc.getId()), doc.getId())
            );
        }
        return documentRepository.updateStatusById(doc.getId(), DocumentStatus.SUBMITTED)
            .then(historyService.createEntry(initiator, doc.getId(), DocumentAction.SUBMIT))
            .thenReturn(new DocumentOpResult(doc.getId(), OperationStatus.SUCCESS));
    }

    private Mono<DocumentOpResult> approveDocument(Document doc, String initiator) {
        if (doc.getStatus() != DocumentStatus.SUBMITTED) {
            return Mono.error(
                new StatusChangeException("Can't approve document %s".formatted(doc.getId()),
                    doc.getId())
            );
        }
        return documentRepository.updateStatusById(doc.getId(), DocumentStatus.APPROVED)
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.when(
                historyService.createEntry(initiator, doc.getId(), DocumentAction.APPROVE),
                approvalService.makeEntry(doc.getId())
            ))
            .thenReturn(new DocumentOpResult(doc.getId(), OperationStatus.SUCCESS));
    }
}
