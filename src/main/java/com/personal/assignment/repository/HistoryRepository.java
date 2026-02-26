package com.personal.assignment.repository;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.model.History;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface HistoryRepository extends R2dbcRepository<History, Long> {
    Flux<History> findAllByDocumentId(Long documentId);

    Flux<History> findAllByDocumentIdAndDocumentAction(Long documentId, DocumentAction action);

    Mono<Boolean> existsByDocumentIdAndDocumentAction(Long documentId, DocumentAction action);
}
