package com.personal.assignment.model;

import com.personal.assignment.enums.DocumentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class Document {

    public static final String DOCUMENT_ID = "id";
    public static final String DOCUMENT_NUMBER = "number";
    public static final String DOCUMENT_AUTHOR = "author";
    public static final String DOCUMENT_TITLE = "title";
    public static final String DOCUMENT_STATUS = "status";
    public static final String DOCUMENT_DATE_CREATED = "date_created";
    public static final String DOCUMENT_DATE_UPDATED = "date_updated";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long number;

    private final String author;

    private final String title;

    private final DocumentStatus status;

    private final ZonedDateTime dateCreated;

    private final ZonedDateTime dateUpdated;

    public Document(Long id, Long number, String author,
                    String title, DocumentStatus status,
                    ZonedDateTime dateCreated, ZonedDateTime dateUpdated) {
        this.id = id;
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

    public Long getNumber() {
        return number;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public ZonedDateTime getDateUpdated() {
        return dateUpdated;
    }

    public static class Builder {
        private Long id;
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
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .dateCreated(document.getDateCreated())
                .dateUpdated(document.getDateUpdated());
        }

        public Document build() {
            return new Document(id, number, author, title, status, dateCreated, dateUpdated);
        }
    }
}
