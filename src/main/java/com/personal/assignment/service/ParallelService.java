package com.personal.assignment.service;

import com.personal.assignment.model.response.ParallelApproveResponse;
import reactor.core.publisher.Mono;

public interface ParallelService {
    Mono<ParallelApproveResponse> approveDocumentParallel(
        Long documentId,
        String initiator,
        Integer threads,
        Integer attempts);
}
