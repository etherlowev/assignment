package com.personal.assignment.service.impl;

import com.personal.assignment.enums.DocumentAction;
import com.personal.assignment.model.History;
import com.personal.assignment.repository.HistoryRepository;
import com.personal.assignment.service.HistoryService;
import jakarta.transaction.Transactional;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    private final Logger log = LoggerFactory.getLogger(HistoryServiceImpl.class);

    public HistoryServiceImpl(@Autowired HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public Mono<History> createHistoryEntry(String initiator, Long documentId, DocumentAction action) {
        log.info("createHistoryEntry() >> Creating history entry for document {}", documentId);
        return historyRepository.save(
            History.builder()
                .initiator(initiator)
                .documentId(documentId)
                .documentAction(action)
                .actionDate(ZonedDateTime.now())
                .build()
        );
    }

    @Override
    public Flux<History> getHistory(Long documentId) {
        return historyRepository.findAllByDocumentId(documentId);
    }
}
