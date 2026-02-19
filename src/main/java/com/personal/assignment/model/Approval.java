package com.personal.assignment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Approval {

    public static final String APPROVAL_ID = "id";
    public static final String APPROVAL_DOCUMENT_ID = "documentIds";
    public static final String APPROVAL_DATE = "approvalDate";

    @Id
    private final Long id;

    private final Long documentId;

    private final LocalDateTime approvalDate;

    public Approval(Long id, Long documentId, LocalDateTime approvalDate) {
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

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
}
