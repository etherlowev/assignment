package com.personal.assignment.repository;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.model.History;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface HistoryRepository extends R2dbcRepository<History, Long> {
    Flux<History> findAllByDocumentId(Long documentId);

    @Query("INSERT INTO history (initiator, action_date, document_id, document_action)" +
        " VALUES (:initiator, now(), :documentId, :action) ON CONFLICT DO NOTHING")
    Mono<History> createHistoryEntry(String initiator, Long documentId, DocumentAction action);

    Flux<History> findAllByDocumentIdAndDocumentAction(Long documentId, DocumentAction action);

    Mono<Boolean> existsByDocumentIdAndDocumentAction(Long documentId, DocumentAction action);
}
