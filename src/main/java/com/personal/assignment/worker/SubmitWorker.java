package com.personal.assignment.worker;

import com.personal.assignment.enums.DocumentStatus;
import com.personal.assignment.filter.impl.DocumentFilteredPaging;
import com.personal.assignment.model.Document;
import com.personal.assignment.service.DocumentService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
public class SubmitWorker {

    private final int batchSize;

    private final DocumentService documentService;

    private final Logger log = LoggerFactory.getLogger(SubmitWorker.class);

    public SubmitWorker(@Value("${app.worker.submit.batchSize}") int batchSize,
                        @Autowired DocumentService documentService) {
        this.batchSize = batchSize;
        this.documentService = documentService;
    }

    @Scheduled(initialDelay = 1000, fixedDelayString = "${app.worker.submit.delay}")
    public void submit() {
        log.info("Submitting batch: {}", batchSize);
        documentService.getDocuments(DocumentFilteredPaging.builder()
                .page(1)
                .perPage(batchSize)
                .statuses(List.of(DocumentStatus.DRAFT))
                .build()
            )
            .publishOn(Schedulers.boundedElastic())
            .buffer(50)
            .flatMap(docs -> {
                if (docs.isEmpty()) {
                    return Flux.empty();
                }
                return documentService.submitBatch(
                    docs.stream().map(Document::getId).collect(Collectors.toSet()),
                        "worker"
                    )
                    .doOnError(err -> log.error("Failed batch chunk", err))
                    .onErrorResume(err -> {
                        log.error("Skipping failed chunk", err);
                        return Flux.empty();
                    });
            })
            .doOnComplete(() -> log.info("Completed submitting batch in worker"))
            .blockLast();
    }
}
