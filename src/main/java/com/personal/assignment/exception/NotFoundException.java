package com.personal.assignment.exception;

public class NotFoundException extends RuntimeException {

    private final Long documentId;

    public NotFoundException(String message, Long documentId) {
        super(message);
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }
}
