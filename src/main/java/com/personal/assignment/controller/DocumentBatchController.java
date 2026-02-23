package com.personal.assignment.controller;

import com.personal.assignment.exception.EmptyBodyException;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.impl.BatchDocumentSubmissionBody;
import com.personal.assignment.model.request.impl.BatchDocumentsBody;
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

    @PostMapping("/create")
    public Mono<ResponseEntity<List<Document>>> creatBatch(@RequestBody BatchDocumentsBody body) {
        if (body.author() == null || body.titles() == null || body.titles().isEmpty()) {
            throw new EmptyBodyException("No author or titles provided");
        }

        return documentService.createDocumentBatch(body.author(), body.titles())
            .collectList()
            .map(documents -> ResponseEntity.ok().body(documents));
    }

    @PostMapping("/submit")
    public Mono<ResponseEntity<List<DocumentOpResult>>> submitDocument(@RequestBody BatchDocumentSubmissionBody body) {

        if (body == null) {
            throw new EmptyBodyException("No input provided");
        }
        if (body.documentIds().isEmpty()) {
            throw new EmptyBodyException("No documents to submit provided");
        }
        if (body.initiator() == null || body.initiator().isBlank()) {
            throw new EmptyBodyException("No initiator provided");
        }
        return documentService.submitBatch(body.documentIds(), body.initiator())
            .collectList()
            .map(list -> ResponseEntity.ok().body(list));
    }

}
