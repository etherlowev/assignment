package com.personal.assignment.enums;

public enum DocumentStatus {
    DRAFT("draft"),
    SUBMITTED("submitted"),
    APPROVED("approved");

    private final String value;

    DocumentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
