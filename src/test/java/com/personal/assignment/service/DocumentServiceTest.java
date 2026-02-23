package com.personal.assignment.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ApprovalService approvalService;
    private final HistoryService historyService;
    private final DocumentRepository documentRepository;
    private final ApprovalRepository approvalRepository;
    private final HistoryRepository historyRepository;

    public DocumentServiceTest(@Autowired DocumentService documentService,
                               @Autowired ApprovalService approvalService,
                               @Autowired HistoryService historyService,
                               @Autowired DocumentRepository documentRepository,
                               @Autowired ApprovalRepository approvalRepository,
                               @Autowired HistoryRepository historyRepository) {
        this.documentService = documentService;
        this.approvalService = approvalService;
        this.historyService = historyService;
        this.documentRepository = documentRepository;
        this.approvalRepository = approvalRepository;
        this.historyRepository = historyRepository;
    }

    @AfterEach
    public void tearDown() {
        documentRepository.deleteAll().block();
        approvalRepository.deleteAll().block();
        historyRepository.deleteAll().block();
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

        assertEquals(amount, getDocuments(200).size());
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

        documentService.submitBatch(documentIds, initiator).collectList().block();

        assertTrue(getDocuments(100).stream().allMatch(doc -> doc.getStatus() == DocumentStatus.SUBMITTED));
    }

    @Test
    public void createAndApproveDocumentTest() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();

        assertNotNull(document);
        assertEquals(DocumentStatus.DRAFT, document.getStatus());

        DocumentOpResult submitRes = documentService.submitDocumentById(document.getId(), author).block();
        assertNotNull(submitRes);
        assertEquals(OperationStatus.SUCCESS, submitRes.status());

        DocumentWithHistory docWIthHistory = documentService.getDocumentById(document.getId()).block();

        assertNotNull(docWIthHistory);

        document = docWIthHistory.getDocument();
        assertEquals(DocumentStatus.SUBMITTED, document.getStatus());

        List<History> histories = historyService.getHistory(document.getId()).collectList().block();
        assertNotNull(histories);
        assertEquals(1, histories.size());
        assertTrue(histories.stream().anyMatch(history -> history.getDocumentAction() == DocumentAction.SUBMIT));

        DocumentOpResult approveRes = approvalService.approveDocumentById(document.getId(), author).block();
        assertNotNull(approveRes);
        assertEquals(OperationStatus.SUCCESS, approveRes.status());

        document = Objects.requireNonNull(documentService.getDocumentById(document.getId()).block())
            .getDocument();

        assertEquals(DocumentStatus.APPROVED, document.getStatus());

        histories = historyService.getHistory(document.getId()).collectList().block();
        assertNotNull(histories);
        assertEquals(2, histories.size());
        assertTrue(histories.stream().anyMatch(history -> history.getDocumentAction() == DocumentAction.APPROVE));
    }

    private List<Document> getDocuments(int amount) {
        return Optional.ofNullable(documentService.getDocuments(
            DocumentFilteredPaging.builder().page(1).perPage(amount).build())
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
