package com.personal.assignment.model;

import com.personal.assignment.enums.DocumentAction;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
public class History implements Serializable {

    public static final String HISTORY_ID = "id";
    public static final String HISTORY_INITIATOR = "initiator";
    public static final String HISTORY_ACTION_DATE = "actionDate";
    public static final String HISTORY_DOCUMENT_ID = "documentId";
    public static final String HISTORY_DOCUMENT_ACTION = "documentAction";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String initiator;

    private ZonedDateTime actionDate;

    private Long documentId;

    private DocumentAction documentAction;

    public History() {}

    public History(Long id, Long version, String initiator, ZonedDateTime actionDate,
                   Long documentId,
                   DocumentAction documentAction) {
        this.id = id;
        this.version = version;
        this.initiator = initiator;
        this.actionDate = actionDate;
        this.documentId = documentId;
        this.documentAction = documentAction;
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

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public ZonedDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(ZonedDateTime actionDate) {
        this.actionDate = actionDate;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public DocumentAction getDocumentAction() {
        return documentAction;
    }

    public void setDocumentAction(DocumentAction documentAction) {
        this.documentAction = documentAction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;

        private Long version;

        private String initiator;

        private ZonedDateTime actionDate;

        private Long documentId;

        private DocumentAction documentAction;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
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
                .version(history.getVersion())
                .initiator(history.getInitiator())
                .actionDate(history.getActionDate())
                .documentId(history.getDocumentId())
                .documentAction(history.getDocumentAction());
        }

        public History build() {
            return new History(id, version, initiator, actionDate, documentId, documentAction);
        }
    }
}
