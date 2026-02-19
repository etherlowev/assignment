package com.personal.assignment.repository;

import com.personal.assignment.model.History;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface HistoryRepository extends R2dbcRepository<History, Long> {
    Flux<History> findAllByDocumentId(Long documentId);
}
