package com.personal.assignment.repository.impl;

import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.exception.NotFoundException;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.Paging;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
        return template.update(Query.query(
            Criteria.where(Document.DOCUMENT_ID).is(documentId)),
            Update.update(Document.DOCUMENT_STATUS, status).set(Document.DOCUMENT_DATE_UPDATED, LocalDate.now()),
            Document.class
        ).flatMap(num -> num < 1 ?
            Mono.error(new NotFoundException("Document with id %s was not found".formatted(documentId), documentId)) :
            Mono.just(num)
        );
    }

    @Override
    public Mono<Document> insertDocument(String author, String title,
                                         DocumentStatus status, ZonedDateTime dateCreated,
                                         ZonedDateTime dateUpdated) {

        GenericExecuteSpec spec = databaseClient.sql("""
                INSERT INTO document
                (author, title, status, date_created, date_updated)
                VALUES (:author, :title, :status, :date_created, :date_updated)
                RETURNING id, number, author, title, status, date_created, date_updated""");

        spec = bindNullable(spec, Document.DOCUMENT_AUTHOR, author, String.class);
        spec = bindNullable(spec, Document.DOCUMENT_TITLE, title, String.class);
        spec = bindNullable(spec, Document.DOCUMENT_STATUS, status.toString(), String.class);
        spec = bindNullable(spec, Document.DOCUMENT_DATE_CREATED, dateCreated, ZonedDateTime.class);
        spec = bindNullable(spec, Document.DOCUMENT_DATE_UPDATED, dateUpdated, ZonedDateTime.class);

        return spec.map(row -> Document.builder()
                .id(row.get(Document.DOCUMENT_ID, Long.class))
                .author(row.get(Document.DOCUMENT_AUTHOR, String.class))
                .number(row.get(Document.DOCUMENT_NUMBER, Long.class))
                .title(row.get( Document.DOCUMENT_TITLE, String.class))
                .status(DocumentStatus.valueOf(row.get(Document.DOCUMENT_STATUS, String.class)))
                .dateCreated(row.get(Document.DOCUMENT_DATE_CREATED, ZonedDateTime.class))
                .dateUpdated(row.get(Document.DOCUMENT_DATE_UPDATED, ZonedDateTime.class))
                .build()
            )
            .one();
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec bindspec,
                                                String name,
                                                T value,
                                                Class<T> type) {

        return value == null ? bindspec.bindNull(name, type) : bindspec.bind(name, value);
    }
}
