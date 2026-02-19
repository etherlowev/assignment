package com.personal.assignment.enums;

public enum DocumentAction {
    SUBMIT("submit"),
    APPROVE("approve");

    private String value;

    DocumentAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
