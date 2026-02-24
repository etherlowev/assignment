package com.personal.assignment.model;

import com.personal.assignment.enums.DocumentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
public class Document implements Serializable {

    public static final String DOCUMENT_ID = "id";
    public static final String DOCUMENT_VERSION = "version";
    public static final String DOCUMENT_NUMBER = "number";
    public static final String DOCUMENT_AUTHOR = "author";
    public static final String DOCUMENT_TITLE = "title";
    public static final String DOCUMENT_STATUS = "status";
    public static final String DOCUMENT_DATE_CREATED = "date_created";
    public static final String DOCUMENT_DATE_UPDATED = "date_updated";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long number;

    private String author;

    private String title;

    private DocumentStatus status;

    private ZonedDateTime dateCreated;

    private ZonedDateTime dateUpdated;

    public Document() {}

    public Document(Long id, Long version, Long number, String author, String title,
                    DocumentStatus status, ZonedDateTime dateCreated, ZonedDateTime dateUpdated) {
        this.id = id;
        this.version = version;
        this.number = number;
        this.author = author;
        this.title = title;
        this.status = status;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
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

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ZonedDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(ZonedDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long version;
        private Long number;
        private String author;
        private String title;
        private DocumentStatus status;
        private ZonedDateTime dateCreated;
        private ZonedDateTime dateUpdated;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder number(Long number) {
            this.number = number;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder status(DocumentStatus status) {
            this.status = status;
            return this;
        }

        public Builder dateCreated(ZonedDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public Builder dateUpdated(ZonedDateTime dateUpdated) {
            this.dateUpdated = dateUpdated;
            return this;
        }

        public static Builder of(Document document) {
            return new Builder()
                .id(document.getId())
                .version(document.getVersion())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .dateCreated(document.getDateCreated())
                .dateUpdated(document.getDateUpdated());
        }

        public Document build() {
            return new Document(id, version, number, author, title, status, dateCreated, dateUpdated);
        }
    }
}
