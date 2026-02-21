package com.personal.assignment.controller;

import com.personal.assignment.model.request.impl.ParallelApproveRequestBody;
import com.personal.assignment.model.response.ParallelApproveResponse;
import com.personal.assignment.service.ParallelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/parallel")
public class ParallelController {
    private final ParallelService parallelService;

    public ParallelController(@Autowired ParallelService parallelService) {
        this.parallelService = parallelService;
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<ParallelApproveResponse>> parallelApprove(@RequestBody ParallelApproveRequestBody body) {
        return parallelService.approveDocumentParallel(
                body.documentId(),
                body.initiator(),
                body.threads(),
                body.attempts()
            )
            .map(ResponseEntity::ok);
    }
}
