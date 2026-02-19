package com.personal.assignment.controller;

import com.personal.assignment.exception.EmptyBodyException;
import com.personal.assignment.model.request.BatchDocumentSubmissionBody;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.service.DocumentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/document/batch")
public class DocumentBatchController {
    private final DocumentService documentService;

    public DocumentBatchController(@Autowired DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/submit")
    public Mono<ResponseEntity<List<DocumentOpResult>>> submitDocument(@RequestBody BatchDocumentSubmissionBody body) {
        if (body.documentIds().isEmpty()) {
            throw new EmptyBodyException("No documents to submit provided");
        }
        return documentService.submitBatch(body.documentIds(), body.initiator())
            .collectList()
            .map(list -> ResponseEntity.ok().body(list));
    }

    @PostMapping("/approve")
    public Mono<ResponseEntity<List<DocumentOpResult>>> approveDocument(@RequestBody BatchDocumentSubmissionBody body) {
        if (body.documentIds().isEmpty()) {
            throw new EmptyBodyException("No documents to approve provided");
        }
        return documentService.approveBatch(body.documentIds(), body.initiator())
            .collectList()
            .map(list -> ResponseEntity.ok().body(list));
    }

}
