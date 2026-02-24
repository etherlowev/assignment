package com.personal.assignment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.ZonedDateTime;

@Entity
public class Approval {

    public static final String APPROVAL_ID = "id";
    public static final String APPROVAL_DOCUMENT_ID = "documentId";
    public static final String APPROVAL_DATE = "approvalDate";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Long documentId;

    private ZonedDateTime approvalDate;

    public Approval() {}

    public Approval(Long id, Long version, Long documentId, ZonedDateTime approvalDate) {
        this.id = id;
        this.version = version;
        this.documentId = documentId;
        this.approvalDate = approvalDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public ZonedDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(ZonedDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
}
