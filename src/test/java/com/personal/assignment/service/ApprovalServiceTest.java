package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.model.response.DocumentWithHistory;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import com.personal.assignment.service.impl.ApprovalServiceImpl;
import com.personal.assignment.service.impl.DocumentServiceImpl;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
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
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ApprovalServiceTest {

    @InjectMocks
    private DocumentServiceImpl documentService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    @MockitoSpyBean
    private DocumentRepository documentRepository;

    @MockitoSpyBean
    private HistoryRepository historyRepository;

    @MockitoSpyBean
    private ApprovalRepository approvalRepository;

    @Autowired
    private DatabaseClient databaseClient;

    private final AtomicLong historyId = new AtomicLong();

    @BeforeEach
    public void setup() {
        databaseClient.sql("ALTER SEQUENCE document_id_seq RESTART WITH 1")
            .then()
            .block();

        databaseClient.sql("DELETE FROM document")
            .then()
            .block();
    }

    @AfterEach
    public void tearDown() {}

    @Test
    public void createApprovalEntry() {
        Document doc = documentRepository.insertDocument(
            "a",
            "b",
            DocumentStatus.DRAFT,
            ZonedDateTime.now(),
            null
        ).block();

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
        assertNotNull(dwh.document());
        assertEquals(DocumentStatus.APPROVED, dwh.document().getStatus());
    }

    @Test
    public void approveBatch() {
        List<Document> documents = makeBatchOfDocuments(100);
        assertNotNull(documents);

        Set<Long> documentIds = documents.stream().map(Document::getId).collect(Collectors.toSet());
        String initiator = "test_initiator";

        documentService.submitBatch(documentIds, initiator).blockLast();

        approvalService.approveBatch(documentIds, initiator).blockLast();

        assertTrue(getDocuments(100).stream()
            .allMatch(doc -> doc.getStatus() == DocumentStatus.APPROVED));
    }

    @Test
    public void approveBatchPartial() {
        List<Document> documents = makeBatchOfDocuments(100);
        assertNotNull(documents);

        Set<Long> documentIds = documents.stream().map(Document::getId).collect(Collectors.toSet());
        String initiator = "test_initiator";

        documentService.submitBatch(documentIds, initiator).blockLast();
        List<Document> docs = getDocuments(100);
        assertTrue(docs.stream().allMatch(doc -> doc.getStatus() == DocumentStatus.SUBMITTED));

        doAnswer(invocation -> {
            History input = invocation.getArgument(0);

            if (input.getDocumentId() > 50) {
                throw new NotFoundException("error", input.getDocumentId());
            }

            History savedHistory = History.Builder.of(input).id(historyId.incrementAndGet()).build();
            return Mono.just(savedHistory);
        }).when(historyRepository).save(any(History.class));

        approvalService.approveBatch(documentIds, initiator).blockLast();

        docs = getDocuments(100);

        assertEquals(50, docs.stream()
            .filter(doc -> doc.getStatus() == DocumentStatus.SUBMITTED).count());

        assertEquals(50, docs.stream()
            .filter(doc -> doc.getStatus() == DocumentStatus.APPROVED).count());

        List<Document> rolledBackDocuments = docs.stream()
            .filter(doc -> doc.getStatus() == DocumentStatus.SUBMITTED).toList();


        rolledBackDocuments.forEach(doc -> {
            assertNotEquals(Boolean.TRUE, (approvalRepository.existsByDocumentId(doc.getId())).block());

            assertNotEquals(Boolean.TRUE, historyRepository.existsByDocumentIdAndDocumentAction(
                    doc.getId(),
                    DocumentAction.APPROVE
                ).block()
            );
        });
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
