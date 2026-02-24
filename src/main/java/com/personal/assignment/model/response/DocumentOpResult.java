package com.personal.assignment.model.response;

import com.personal.assignment.enums.OperationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentOpResult(
    @Schema(description = "Идентификатор документа", example = "1") Long documentId,
    @Schema(description = "Результат операции", examples = {"SUCCESS", "CONFLICT", "NOT_FOUND", "ERROR"})
    OperationStatus status) {}
