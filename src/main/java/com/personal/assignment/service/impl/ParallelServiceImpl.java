package com.personal.assignment.service.impl;

import com.personal.assignment.model.response.ParallelApproveResponse;
import com.personal.assignment.service.ApprovalService;
import com.personal.assignment.service.ParallelService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ParallelServiceImpl implements ParallelService {

    private final ApprovalService approvalService;

    private final Logger log = LoggerFactory.getLogger(ParallelServiceImpl.class);

    public ParallelServiceImpl(@Autowired ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Override
    public Mono<ParallelApproveResponse> approveDocumentParallel(
        Long documentId,
        String initiator,
        Integer threads,
        Integer attempts) {

        if (attempts == 0) {
            return Mono.just(
                new ParallelApproveResponse(0L,0L,0L,0L)
            );
        }

        AtomicLong successCounter = new AtomicLong();
        AtomicLong conflictCounter = new AtomicLong();
        AtomicLong errorCounter = new AtomicLong();

        List<Mono<Void>> monos = launchMonos(documentId, initiator, threads, attempts,
            successCounter, conflictCounter, errorCounter);

        return Flux.merge(monos)
            .then(Mono.defer(() -> Mono.just(new ParallelApproveResponse(
                successCounter.get() + errorCounter.get() + conflictCounter.get(),
                successCounter.get(),
                conflictCounter.get(),
                errorCounter.get()
            ))));
    }

    private List<Mono<Void>> launchMonos(Long documentId, String initiator,
                                         int threads, Integer attempts,
                                         AtomicLong successCounter,
                                         AtomicLong conflictCounter,
                                         AtomicLong errorCounter) {
        List<Mono<Void>> monos = new ArrayList<>();
        for (int i = 0 ; i < threads ; i++) {
            monos.add(approvalService.approveDocumentById(documentId, initiator)
                .repeat(Math.max(attempts-1, 0))
                .collectList()
                .doOnNext(list -> list.forEach(opResult -> {
                    switch (opResult.status()) {
                        case SUCCESS:
                            successCounter.incrementAndGet();
                            break;
                        case CONFLICT:
                            conflictCounter.incrementAndGet();
                            break;
                        default:
                            log.error(opResult.status().toString());
                            errorCounter.incrementAndGet();
                            break;
                    }
                }))
                .then()
            );
        }
        return monos;
    }
}
