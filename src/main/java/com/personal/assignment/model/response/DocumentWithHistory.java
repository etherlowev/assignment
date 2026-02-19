package com.personal.assignment.model.response;

import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import java.util.List;

public class DocumentWithHistory {
    private final Document document;
    private final List<History> history;

    public DocumentWithHistory(Document document, List<History> history) {
        this.document = document;
        this.history = history;
    }

    public Document getDocument() {
        return document;
    }

    public List<History> getHistory() {
        return history;
    }
}
