package com.personal.assignment.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.Direction;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        Document pulledDocument = dwh.document();
        assertEquals(author, pulledDocument.getAuthor());
        assertEquals(title, pulledDocument.getTitle());
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
        assertNotNull(dwh.document());
        assertEquals(DocumentStatus.SUBMITTED, dwh.document().getStatus());
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

        document = docWIthHistory.document();
        assertEquals(DocumentStatus.SUBMITTED, document.getStatus());

        List<History> histories = historyService.getHistory(document.getId()).collectList().block();
        assertNotNull(histories);
        assertEquals(1, histories.size());
        assertTrue(histories.stream().anyMatch(history -> history.getDocumentAction() == DocumentAction.SUBMIT));

        DocumentOpResult approveRes = approvalService.approveDocumentById(document.getId(), author).block();
        assertNotNull(approveRes);
        assertEquals(OperationStatus.SUCCESS, approveRes.status());

        document = Objects.requireNonNull(documentService.getDocumentById(document.getId()).block())
            .document();

        assertEquals(DocumentStatus.APPROVED, document.getStatus());

        histories = historyService.getHistory(document.getId()).collectList().block();
        assertNotNull(histories);
        assertEquals(2, histories.size());
        assertTrue(histories.stream().anyMatch(history -> history.getDocumentAction() == DocumentAction.APPROVE));
    }

    @Test
    public void documentDatePagingTest() {
        String author = "test_author";
        ZonedDateTime y2024 = ZonedDateTime.of(2024, 1, 2,
            0, 0, 0, 0,
            ZoneId.systemDefault()
        );

        ZonedDateTime y2025 = ZonedDateTime.of(2025, 1, 2,
            0, 0, 0, 0,
            ZoneId.systemDefault()
        );

        createDocumentsForPaging(author);

        DocumentFilteredPaging onePaging = DocumentFilteredPaging.builder()
            .page(1)
            .perPage(1).build();

        assertEquals(1, documentService.getDocuments(onePaging).collectList().block().size());

        DocumentFilteredPaging dateCreatedPaging = DocumentFilteredPaging.builder()
            .createdAfter(y2025)
            .build();

        assertEquals(1,
            documentService.getDocuments(dateCreatedPaging).collectList().block().size());

        DocumentFilteredPaging dateUpdatedPaging = DocumentFilteredPaging.builder()
            .updatedAfter(y2024)
            .build();

        assertEquals(2,
            documentService.getDocuments(dateUpdatedPaging).collectList().block().size());
    }

    @Test
    public void documentStatusPagingTest() {
        String author = "test_author";
        createDocumentsForPaging(author);
        DocumentFilteredPaging statusesPaging = DocumentFilteredPaging.builder()
            .statuses(List.of(DocumentStatus.DRAFT, DocumentStatus.SUBMITTED))
            .build();

        assertEquals(2, documentService.getDocuments(statusesPaging).collectList().block().size());
    }

    @Test
    public void authorsPagingTest() {
        String author = "test_author";
        createDocumentsForPaging(author);

        DocumentFilteredPaging authorsPaging = DocumentFilteredPaging.builder()
            .authors(List.of(author))
            .build();

        assertEquals(3, documentService.getDocuments(authorsPaging).collectList().block().size());
    }

    @Test
    public void statusPagingTest() {
        DocumentFilteredPaging sortCheck = DocumentFilteredPaging.builder()
            .sort(Document.DOCUMENT_ID)
            .direction(Direction.DESC)
            .build();

        List<Document> docs = documentService.getDocuments(sortCheck).collectList().block();
        assertNotNull(docs);
        for (int i = 1 ; i < docs.size() ; i++) {
            assertTrue(docs.get(i-1).getId() > docs.get(i).getId());
        }
    }


    private void createDocumentsForPaging(String author) {
        ZonedDateTime y2024 = ZonedDateTime.of(2024, 1, 2,
            0, 0, 0, 0,
            ZoneId.systemDefault()
        );

        ZonedDateTime y2025 = ZonedDateTime.of(2025, 1, 2,
            0, 0, 0, 0,
            ZoneId.systemDefault()
        );

        ZonedDateTime y2026 = ZonedDateTime.of(2026, 1, 2,
            0, 0, 0, 0,
            ZoneId.systemDefault()
        );

        createDocumentWithDate(author, "title1", DocumentStatus.DRAFT, y2026, null);
        createDocumentWithDate(author, "title2", DocumentStatus.SUBMITTED, y2025, y2026);
        createDocumentWithDate(author, "title3", DocumentStatus.APPROVED, y2024, y2025);
    }

    private void createDocumentWithDate(String author, String title,
                                        DocumentStatus status,
                                        ZonedDateTime dateCreated,
                                        ZonedDateTime dateUpdated) {
        documentRepository.save(
                Document.builder()
                    .author(author)
                    .title(title)
                    .status(status)
                    .dateCreated(dateCreated)
                    .dateUpdated(dateUpdated)
                    .build()
            )
            .block();
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
