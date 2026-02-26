package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class HistoryServiceTest {

    private final HistoryService historyService;
    private final HistoryRepository historyRepository;
    private final DocumentRepository documentRepository;

    public HistoryServiceTest(@Autowired HistoryService historyService,
                              @Autowired HistoryRepository historyRepository,
                              @Autowired DocumentRepository documentRepository) {
        this.historyService = historyService;
        this.historyRepository = historyRepository;
        this.documentRepository = documentRepository;
    }

    @AfterEach
    public void tearDown() {
        historyRepository.deleteAll().block();
        documentRepository.deleteAll().block();
    }

    @Test
    public void createHistoryEntry() {
        Document doc = documentRepository.save(
            Document.builder()
                .author("author")
                .title("title")
                .status(DocumentStatus.APPROVED)
                .dateCreated(ZonedDateTime.now())
                .build()
            )
            .block();

        assertNotNull(doc);

        String initiator = doc.getAuthor();
        Long documentId = doc.getId();
        DocumentAction action = DocumentAction.APPROVE;

        historyService.createHistoryEntry(initiator, documentId, action).block();

        History history = historyService.getHistory(doc.getId()).blockLast();
        assertNotNull(history);
        assertEquals(doc.getId(), history.getDocumentId());
    }
}
