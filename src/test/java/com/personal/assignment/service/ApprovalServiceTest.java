package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import com.personal.assignment.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ApprovalServiceTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private DocumentService documentService;

    private ApprovalService approvalService;

    @Autowired
    private HistoryRepository historyRepository;


    @AfterEach
    public void tearDown() {
        approvalRepository.deleteAll().block();
        documentRepository.deleteAll().block();
    }

    @BeforeEach
    public void setup() {
        approvalService = Mockito.spy(new ApprovalServiceImpl(
            approvalRepository, documentRepository, historyRepository
        ));
    }


    @Test
    public void createApprovalEntry() {
        Document doc = documentRepository.insertDocument("a","b", DocumentStatus.DRAFT,
            ZonedDateTime.now(), null).block();

        assertNotNull(doc);
        DocumentOpResult result = approvalService.createApprovalEntry(doc.getId()).block();
        assertNotNull(result);
        assertEquals(result.status(), OperationStatus.SUCCESS);
    }


    @Test
    public void approveDocumentById() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();
        assertNotNull(document);
        documentService.submitDocumentById(document.getId(), "test").block();

        approvalService.approveDocumentById(document.getId(), "test").block();

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

        approvalService.approveBatch(documentIds, initiator).blockLast();

        assertTrue(getDocuments(100).stream().allMatch(doc -> doc.getStatus() == DocumentStatus.APPROVED));
    }

    @Test
    public void rollbackOnApproveTest() {
        String author = "test_author";
        String title = "test_title";
        Document document = documentService.createDocument(author, title).block();
        assertNotNull(document);
        documentService.submitDocumentById(document.getId(), "test").block();

        when(approvalService.createApprovalEntry(document.getId())).thenThrow(new RuntimeException("fake error"));

        try {
            approvalService.approveDocumentById(document.getId(), author).block();
        }
        catch (RuntimeException ignored) {}

        Document pulledDoc = documentRepository.findById(document.getId()).block();
        assertNotNull(pulledDoc);
        assertEquals(DocumentStatus.SUBMITTED, pulledDoc.getStatus());

        assertEquals(1, historyRepository.count().block());
    }

    private List<Document> getDocuments(int amount) {
        return Optional.ofNullable(documentService.getDocuments(
                    DocumentFilteredPaging.builder().page(1).perPage(amount).build())
                .collectList().block()
            )
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
