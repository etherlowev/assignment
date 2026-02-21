package com.personal.assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.personal.assignment.configuration.TestcontainersConfiguration;
import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class ApprovalServiceTest {

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApprovalRepository approvalRepository;


    @AfterEach
    public void tearDown() {
        approvalRepository.deleteAll().block();
    }


    @Test
    public void makeEntry() {
        Document doc = documentRepository.insertDocument("a","b", DocumentStatus.DRAFT,
            ZonedDateTime.now(), null).block();

        assertNotNull(doc);
        DocumentOpResult result = approvalService.makeEntry(doc.getId()).block();
        assertNotNull(result);
        assertEquals(result.status(), OperationStatus.SUCCESS);
    }
}
