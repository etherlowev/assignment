package com.personal.assignment.controller;

import com.personal.assignment.model.request.impl.ParallelApproveRequestBody;
import com.personal.assignment.model.response.ParallelApproveResponse;
import com.personal.assignment.service.ParallelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/parallel")
public class ParallelController {
    private final ParallelService parallelService;

    public ParallelController(@Autowired ParallelService parallelService) {
        this.parallelService = parallelService;
    }

    @Operation(summary = "Параллельное согласование документа",
        description = "Параллельное согласование документа из нескольких потоков"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Операция завершена"),
        @ApiResponse(responseCode = "500", description = "Неизвестная ошибка")
    })
    @PostMapping("/approve")
    public Mono<ResponseEntity<ParallelApproveResponse>> parallelApprove(@RequestBody ParallelApproveRequestBody body) {
        return parallelService.approveDocumentParallel(
                body.documentId(),
                body.initiator(),
                body.threads(),
                body.attempts()
            )
            .map(ResponseEntity::ok);
    }
}
