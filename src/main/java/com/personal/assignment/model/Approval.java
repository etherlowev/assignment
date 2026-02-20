package com.personal.assignment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class Approval {

    public static final String APPROVAL_ID = "id";
    public static final String APPROVAL_DOCUMENT_ID = "documentIds";
    public static final String APPROVAL_DATE = "approvalDate";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    private final Long documentId;

    private final ZonedDateTime approvalDate;

    public Approval(Long id, Long documentId, ZonedDateTime approvalDate) {
        this.id = id;
        this.documentId = documentId;
        this.approvalDate = approvalDate;
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public ZonedDateTime getApprovalDate() {
        return approvalDate;
    }
}
