package com.personal.assignment.controller;

import com.personal.assignment.model.Approval;
import com.personal.assignment.service.ApprovalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(@Autowired ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/list")
    public Mono<ResponseEntity<List<Approval>>> approve() {
        return approvalService.getApprovals()
            .collectList()
            .map(ResponseEntity::ok);
    }
}
