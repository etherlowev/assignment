package com.personal.assignment.repository.impl;

import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.exception.StatusChangeException;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.Paging;
import io.r2dbc.spi.Readable;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {
    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;

    public DocumentRepositoryImpl(@Autowired R2dbcEntityTemplate template,
                                  @Autowired DatabaseClient databaseClient) {
        this.template = template;
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<Document> getPage(Criteria criteria, Paging paging) {
        return template.select(Document.class)
            .matching(
                Query.query(criteria)
                    .limit(paging.getPageSize())
                    .offset(paging.getOffset())
                    .sort(paging.getSort())
            ).all();
    }

    @Override
    public Mono<Document> findById(Long id) {
        return template.selectOne(Query.query(Criteria.where("id").is(id)), Document.class)
            .switchIfEmpty(Mono.error(new NotFoundException("Document with id %s was not found".formatted(id), id)));
    }

    @Override
    public Mono<Document> findByNumber(Long number) {
        return template.selectOne(Query.query(Criteria.where("number").is(number)), Document.class)
            .switchIfEmpty(Mono.error(new NotFoundException("Document with number %s was not found"
                .formatted(number), number)));
    }

    @Override
    public Mono<Void> deleteAll() {
        return databaseClient.sql("DELETE FROM document").then();
    }

    @Override
    public Mono<Long> updateStatusById(Long documentId, DocumentStatus status) {
        return template.selectOne(Query.query(Criteria.where(Document.DOCUMENT_ID).is(documentId)), Document.class)
            .switchIfEmpty(Mono.error(new NotFoundException("Document with id %s was not found"
                .formatted(documentId), documentId))
            )
            .flatMap(document -> document.getStatus() == status ?
                Mono.error(new StatusChangeException("Status is already set", documentId)) :
                Mono.just(document)
            )
            .flatMap(document -> template.update(
                    Query.query(
                        Criteria.where(Document.DOCUMENT_ID).is(documentId)
                            .and(Document.DOCUMENT_VERSION).is(document.getVersion())
                    ),
                    Update.update(Document.DOCUMENT_STATUS, status)
                        .set(Document.DOCUMENT_DATE_UPDATED, ZonedDateTime.now())
                        .set(Document.DOCUMENT_VERSION, document.getVersion()+1L),
                    Document.class
                )
            )
            .flatMap(rowsChanged -> rowsChanged < 1 ?
                Mono.error(new StatusChangeException("Could not change status of document with id %s"
                    .formatted(documentId), documentId)
                ) :
                Mono.just(rowsChanged)
            );
    }

    @Override
    public Mono<Document> save(Document document) {

        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO document
                (author, title, status, date_created, date_updated)
                VALUES (:author, :title, :status, :date_created, :date_updated)
                RETURNING id, number, author, title, status, date_created, date_updated, version""");

        spec = bindNullable(spec, Document.DOCUMENT_AUTHOR, document.getAuthor(), String.class);
        spec = bindNullable(spec, Document.DOCUMENT_TITLE, document.getTitle(), String.class);
        spec = bindNullable(spec, Document.DOCUMENT_STATUS,
            Optional.ofNullable(document.getStatus()).map(Enum::toString).orElse(null), String.class);

        spec = bindNullable(spec, Document.DOCUMENT_DATE_CREATED, document.getDateCreated(), ZonedDateTime.class);
        spec = bindNullable(spec, Document.DOCUMENT_DATE_UPDATED, document.getDateUpdated(), ZonedDateTime.class);

        return spec.map(this::buildFromRow).one();
    }

    private Document buildFromRow(Readable row) {
        return Document.builder()
            .id(row.get(Document.DOCUMENT_ID, Long.class))
            .author(row.get(Document.DOCUMENT_AUTHOR, String.class))
            .number(row.get(Document.DOCUMENT_NUMBER, Long.class))
            .title(row.get( Document.DOCUMENT_TITLE, String.class))
            .status(Optional.ofNullable(row.get(Document.DOCUMENT_STATUS, String.class))
                .map(DocumentStatus::valueOf)
                .orElse(null)
            )
            .dateCreated(row.get(Document.DOCUMENT_DATE_CREATED, ZonedDateTime.class))
            .dateUpdated(row.get(Document.DOCUMENT_DATE_UPDATED, ZonedDateTime.class))
            .version(row.get(Document.DOCUMENT_VERSION, Long.class))
            .build();
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec bindspec,
                                                String name,
                                                T value,
                                                Class<T> type) {

        return value == null ? bindspec.bindNull(name, type) : bindspec.bind(name, value);
    }
}
