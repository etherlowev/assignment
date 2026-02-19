package com.personal.assignment.exception;

public class StatusChangeException extends RuntimeException {

    private final Long documentId;

    public StatusChangeException(String message, Long documentId) {
        super(message);
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }
}
