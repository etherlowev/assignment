package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.model.Document;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.repository.HistoryRepository;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;

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
        Mono.when(historyRepository.deleteAll(), documentRepository.deleteAll()).block();
    }

    @Test
    public void createHistoryEntry() {

        Document doc = documentRepository.insertDocument("author", "title", DocumentStatus.APPROVED,
            ZonedDateTime.now(), null).block();

        assertNotNull(doc);

        String initiator = doc.getAuthor();
        Long documentId = doc.getId();
        DocumentAction action = DocumentAction.APPROVE;

        historyService.createHistoryEntry(initiator, documentId, action).block();

        assertEquals(doc.getId(), historyService.getHistory(doc.getId()).blockLast().getDocumentId());
    }
}
