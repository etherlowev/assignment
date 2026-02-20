package com.personal.assignment.repository;

import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.model.Document;
import com.personal.assignment.model.request.Paging;
import java.time.ZonedDateTime;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentRepository {
    Mono<Long> updateStatusById(Long documentId, DocumentStatus status);

    Mono<Document> insertDocument(String author,
                                  String title,
                                  DocumentStatus status,
                                  ZonedDateTime dateCreated,
                                  ZonedDateTime dateUpdated);

    Flux<Document> getPage(Criteria criteria, Paging paging);

    Mono<Document> findById(Long id);

    Mono<Document> findByNumber(Long number);
}
