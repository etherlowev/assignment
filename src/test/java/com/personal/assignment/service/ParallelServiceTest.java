package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.response.ParallelApproveResponse;
import com.personal.assignment.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class ParallelServiceTest {
    @Autowired
    private ParallelService parallelService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @AfterEach
    public void tearDown() {
        documentRepository.deleteAll().block();
    }

    @Test
    public void parallelServiceTest() {
        Document document = documentService.createDocument("author", "title").block();
        assertNotNull(document);

        documentService.submitDocumentById(document.getId(), "initiator").block();

        ParallelApproveResponse result = parallelService.approveDocumentParallel(
            document.getId(), "initiator", 4, 20
        ).block();

        assertNotNull(result);
        assertEquals(1, result.successfulAttempts());
        assertEquals(79, result.conflictedAttempts());
        assertEquals(0, result.errorAttempts());
        assertEquals(80, result.total());
    }
}
