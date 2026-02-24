package com.personal.assignment.controller;

import com.personal.assignment.exception.EmptyBodyException;
import com.personal.assignment.model.Approval;
import com.personal.assignment.model.request.impl.BatchDocumentSubmissionBody;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    private final Logger log = LoggerFactory.getLogger(ApprovalController.class);

    public ApprovalController(@Autowired ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Operation(summary = "Вывести список утверждений", description = "Возвращает список утверждений")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешно выведен список утверждений"),
        @ApiResponse(responseCode = "500", description = "Неизвестная ошибка")
    })
    @GetMapping("/list")
    public Mono<ResponseEntity<List<Approval>>> listApprovals() {
        return approvalService.getApprovals()
            .collectList()
            .map(ResponseEntity::ok);
    }

    @Operation(summary = "Утверждение пачки документов",
        description = "Возвращает список результатов утверждений по документам"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Операция выполнена"),
        @ApiResponse(responseCode = "500", description = "Неизвестная ошибка")
    })
    @PostMapping("/approve")
    public Mono<ResponseEntity<List<DocumentOpResult>>> approveDocument(@RequestBody
                                                                        BatchDocumentSubmissionBody body) {

        if (body == null) {
            throw new EmptyBodyException("No input provided");
        }
        if (body.documentIds().isEmpty()) {
            throw new EmptyBodyException("No documents to approve provided");
        }
        if (body.initiator() == null || body.initiator().isBlank()) {
            throw new EmptyBodyException("No initiator provided");
        }
        log.info("approveBatch() >> beginning operation on {} documents", body.documentIds().size());
        return approvalService.approveBatch(body.documentIds(), body.initiator())
            .collectList()
            .map(list -> ResponseEntity.ok().body(list));
    }
}
