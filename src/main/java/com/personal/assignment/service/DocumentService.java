package com.personal.assignment.service;

import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.impl.DocumentCreationBody;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentService {
    Mono<Document> createDocument(DocumentCreationBody body);

    Mono<DocumentOpResult> submitDocumentById(Long id, String initiator);

    Flux<DocumentOpResult> submitBatch(Set<Long> documentIds, String initiator);

    Mono<DocumentOpResult> approveDocumentById(Long id, String initiator);

    Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator);

    Mono<DocumentWithHistory> getDocumentById(Long id);

    Flux<Document> getDocuments(DocumentFilteredPaging filteredPaging);
}
