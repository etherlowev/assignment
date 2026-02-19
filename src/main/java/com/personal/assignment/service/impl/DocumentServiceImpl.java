package com.personal.assignment.service.impl;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.service.ApprovalService;
import com.personal.assignment.service.DocumentService;
import com.personal.assignment.service.HistoryService;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.DocumentCreationBody;
import com.personal.assignment.model.request.Paging;
import java.time.LocalDateTime;
import java.util.Set;
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

    public DocumentServiceImpl(@Autowired DocumentRepository documentRepository,
                               @Autowired HistoryService historyService,
                               @Autowired ApprovalService approvalService) {
        this.documentRepository = documentRepository;
        this.historyService = historyService;
        this.approvalService = approvalService;
    }

    @Override
    public Mono<Document> createDocument(DocumentCreationBody body) {
        return documentRepository.insertDocument(
            body.author(),
            body.title(),
            DocumentStatus.DRAFT,
            LocalDateTime.now(),
            null
        );
    }

    @Override
    public Mono<DocumentOpResult> submitDocumentById(Long documentId, String initiator) {
        return documentRepository.findById(documentId)
            .publishOn(Schedulers.boundedElastic())
            .flatMap(doc -> submitDocument(doc, initiator))
            .onErrorResume(NotFoundException.class,
                ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)))
            .onErrorResume(StatusChangeException.class,
                ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.CONFLICT)))
            .onErrorResume(Throwable.class,
                ex -> Mono.just(new DocumentOpResult(documentId, OperationStatus.ERROR)));
    }

    @Override
    public Flux<DocumentOpResult> submitBatch(Set<Long> documentIds, String initiator) {
        DocumentAction action = DocumentAction.SUBMIT;
        log.info("Batch creating history for {} objects, initiated by {} with action {}",
            documentIds.size(), initiator, action);

        return Flux.fromIterable(documentIds)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(documentId -> this.submitDocumentById(documentId, initiator))
            .sequential(10)
            .doFinally(signalType -> log.info("Finished batch {} on {} objects", action, documentIds.size()));
    }

    @Override
    public Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator) {
        log.info("beginning operation on {} documents", documentIds.size());
        return Flux.fromIterable(documentIds)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(documentId -> this.approveDocumentById(documentId, initiator))
            .sequential(10)
            .doFinally(signalType -> log.info("finished operation on {} documents", documentIds.size()));
    }

    @Override
    public Mono<DocumentOpResult> approveDocumentById(Long documentId, String initiator) {
        log.info("beginning operation on {} document", documentId);
        return documentRepository.findById(documentId)
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
            .doFinally(signalType -> log.info("finished operation on {} document", documentId));
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
    public Flux<Document> getDocuments(Paging paging) {
        return documentRepository.getPage(paging);
    }

    private Mono<DocumentOpResult> submitDocument(Document doc, String initiator) {
        if (doc.getStatus() != DocumentStatus.DRAFT) {
            return Mono.error(
                new StatusChangeException("Can't submit document %s".formatted(doc.getId()), doc.getId())
            );
        }
        return documentRepository.updateStatusById(doc.getId(), DocumentStatus.SUBMITTED)
            .then(Mono.when(
                historyService.createEntry(initiator, doc.getId(), DocumentAction.SUBMIT),
                approvalService.makeEntry(doc.getId())
            ))
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
