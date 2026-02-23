package com.personal.assignment.service;

import com.personal.assignment.model.Approval;
import com.personal.assignment.model.response.DocumentOpResult;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApprovalService {
    Mono<DocumentOpResult> createApprovalEntry(Long documentId);

    Mono<DocumentOpResult> approveDocumentById(Long id, String initiator);

    Flux<DocumentOpResult> approveBatch(Set<Long> documentIds, String initiator);

    Flux<Approval> getApprovals();
}
