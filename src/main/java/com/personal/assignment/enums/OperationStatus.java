package com.personal.assignment.enums;

public enum OperationStatus {
    SUCCESS("success"),
    CONFLICT("conflict"),
    NOT_FOUND("notFound"),
    ERROR("error");

    private final String value;

    OperationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
