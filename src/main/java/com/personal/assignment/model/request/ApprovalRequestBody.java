package com.personal.assignment.model.request;

public class ApprovalRequestBody {
    private final Long documentId;

    public ApprovalRequestBody(Long documentId) {
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }
}
