package com.personal.assignment.service;

import com.personal.assignment.model.Approval;
import com.personal.assignment.model.response.DocumentOpResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ApprovalService {
    Mono<DocumentOpResult> makeEntry(Long documentId);

    Flux<Approval> getApprovals();
}
