package com.personal.assignment.controller;

import com.personal.assignment.exception.EmptyBodyException;
import com.personal.assignment.model.Approval;
import com.personal.assignment.model.request.impl.BatchDocumentSubmissionBody;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.service.ApprovalService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    private final Logger log = LoggerFactory.getLogger(ApprovalController.class);

    public ApprovalController(@Autowired ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/list")
    public Mono<ResponseEntity<List<Approval>>> approve() {
        return approvalService.getApprovals()
            .collectList()
            .map(ResponseEntity::ok);
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<List<DocumentOpResult>>> approveDocument(@RequestBody
                                                                        BatchDocumentSubmissionBody body) {

        if (body == null) {
            throw new EmptyBodyException("No input provided");
        }
        if (body.documentIds().isEmpty()) {
            throw new EmptyBodyException("No documents to approve provided");
        }
        if (body.initiator() == null || body.initiator().isBlank()) {
            throw new EmptyBodyException("No initiator provided");
        }
        log.info("approveBatch() >> beginning operation on {} documents", body.documentIds().size());
        return approvalService.approveBatch(body.documentIds(), body.initiator())
            .collectList()
            .map(list -> ResponseEntity.ok().body(list));
    }
}
