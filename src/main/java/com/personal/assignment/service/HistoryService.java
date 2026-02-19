package com.personal.assignment.service;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.model.History;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HistoryService {
    Mono<History> createEntry(String initiator, Long documentId, DocumentAction action);

    Flux<History> getHistory(Long documentId);
}
