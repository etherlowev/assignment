package com.personal.assignment.controller;

import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.service.DocumentService;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.impl.DocumentCreationBody;
import com.personal.assignment.model.request.impl.DocumentSubmissionBody;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/document")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(@Autowired DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<Document>> createDocument(@RequestBody DocumentCreationBody body) {
        return documentService.createDocument(body.author(), body.title())
            .map(document -> ResponseEntity.ok().body(document));
    }

    @GetMapping("/get/{id}")
    public Mono<ResponseEntity<DocumentWithHistory>> getDocumentWithHistory(@PathVariable("id") Long id) {
        return documentService.getDocumentById(id)
            .map(document -> ResponseEntity.ok().body(document));
    }

    @GetMapping("/list")
    public Mono<ResponseEntity<List<Document>>> getDocuments(@ModelAttribute DocumentFilteredPaging filteredPaging) {
        return documentService.getDocuments(filteredPaging)
            .collectList()
            .map(document -> ResponseEntity.ok().body(document));
    }

    @PostMapping("/submit")
    public Mono<ResponseEntity<DocumentOpResult>> submitDocument(@RequestBody DocumentSubmissionBody body) {
        return documentService.submitDocumentById(body.documentId(), body.initiator())
            .map(document -> ResponseEntity.ok().body(document));
    }
}
