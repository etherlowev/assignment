package com.personal.assignment.model;

import com.personal.assignment.enums.DocumentAction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class History {

    public static final String HISTORY_ID = "id";
    public static final String HISTORY_INITIATOR = "initiator";
    public static final String HISTORY_ACTION_DATE = "actionDate";
    public static final String HISTORY_DOCUMENT_ID = "documentIds";
    public static final String HISTORY_DOCUMENT_ACTION = "documentAction";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    private final String initiator;

    private final ZonedDateTime actionDate;

    private final Long documentId;

    private final DocumentAction documentAction;

    public History(Long id, String initiator, ZonedDateTime actionDate,
                   Long documentId, DocumentAction documentAction) {
        this.id = id;
        this.initiator = initiator;
        this.actionDate = actionDate;
        this.documentId = documentId;
        this.documentAction = documentAction;
    }

    public Long getId() {
        return id;
    }

    public String getInitiator() {
        return initiator;
    }

    public ZonedDateTime getActionDate() {
        return actionDate;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public DocumentAction getDocumentAction() {
        return documentAction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;

        private String initiator;

        private ZonedDateTime actionDate;

        private Long documentId;

        private DocumentAction documentAction;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder initiator(String initiator) {
            this.initiator = initiator;
            return this;
        }

        public Builder actionDate(ZonedDateTime actionDate) {
            this.actionDate = actionDate;
            return this;
        }

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder documentAction(DocumentAction documentAction) {
            this.documentAction = documentAction;
            return this;
        }

        public static Builder of(History history) {
            return new Builder()
                .id(history.getId())
                .initiator(history.getInitiator())
                .actionDate(history.getActionDate())
                .documentId(history.getDocumentId())
                .documentAction(history.getDocumentAction());
        }

        public History build() {
            return new History(id, initiator, actionDate, documentId, documentAction);
        }
    }
}
