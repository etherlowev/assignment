package com.personal.assignment.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.DocumentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class DocumentServiceTest {
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    public DocumentServiceTest(@Autowired DocumentService documentService,
                               @Autowired DocumentRepository documentRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
    }

    @AfterEach
    public void tearDown() {
        documentRepository.deleteAll().block();
    }

    @Test
    public void createDocument() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();
        assertNotNull(document);

        assertNotNull(document.getAuthor());

        DocumentWithHistory dwh = documentService.getDocumentById(document.getId()).block();
        assertNotNull(dwh);
        Document pulledDocument = dwh.getDocument();
        assertEquals(pulledDocument.getAuthor(), author);
        assertEquals(pulledDocument.getTitle(), title);
    }

    @Test
    public void createDocumentBatch() {
        int amount = 100;
        makeBatchOfDocuments(amount);

        assertEquals(getDocuments(200).size(), amount);
    }

    @Test
    public void submitDocumentById() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();
        assertNotNull(document);

        documentService.submitDocumentById(document.getId(), "test").block();

        DocumentWithHistory dwh = documentService.getDocumentById(document.getId()).block();
        assertNotNull(dwh);
        assertNotNull(dwh.getDocument());
        assertEquals(DocumentStatus.SUBMITTED, dwh.getDocument().getStatus());
    }

    @Test
    public void submitBatch() {
        List<Document> documents = makeBatchOfDocuments(100);

        assertNotNull(documents);

        Set<Long> documentIds = documents.stream().map(Document::getId).collect(Collectors.toSet());
        String initiator = "test_initiator";

        List<DocumentOpResult> res = documentService.submitBatch(documentIds, initiator).collectList().block();

        assertTrue(getDocuments(100).stream().allMatch(doc -> doc.getStatus() == DocumentStatus.SUBMITTED));
    }

    @Test
    public void approveDocumentById() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();
        assertNotNull(document);
        documentService.submitDocumentById(document.getId(), "test").block();

        documentService.approveDocumentById(document.getId(), "test").block();

        DocumentWithHistory dwh = documentService.getDocumentById(document.getId()).block();
        assertNotNull(dwh);
        assertNotNull(dwh.getDocument());
        assertEquals(DocumentStatus.APPROVED, dwh.getDocument().getStatus());
    }

    @Test
    public void approveBatch() {
        List<Document> documents = makeBatchOfDocuments(100);
        assertNotNull(documents);

        Set<Long> documentIds = documents.stream().map(Document::getId).collect(Collectors.toSet());
        String initiator = "test_initiator";

        documentService.submitBatch(documentIds, initiator).blockLast();

        documentService.approveBatch(documentIds, initiator).blockLast();

        assertTrue(getDocuments(100).stream().allMatch(doc -> doc.getStatus() == DocumentStatus.APPROVED));
    }

    private List<Document> getDocuments(int amount) {
        return Optional.ofNullable(documentService.getDocuments(
            DocumentFilteredPaging.builder().page(1).perPage(100).build())
            .collectList().block())
            .orElse(new ArrayList<>());
    }

    private List<Document> makeBatchOfDocuments(int amount) {
        String author = "test_author";
        List<String> titles = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            titles.add("test_title" + i);
        }

        return documentService.createDocumentBatch(author, titles).collectList().block();
    }
}
