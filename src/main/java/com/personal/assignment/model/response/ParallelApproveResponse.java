package com.personal.assignment.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ParallelApproveResponse(
    @Schema(description = "Общее число попыток", example = "20") Long total,
    @Schema(description = "Успешные попытки", example = "1") Long successfulAttempts,
    @Schema(description = "Конфликтные попытки", example = "18") Long conflictedAttempts,
    @Schema(description = "Попытки с ошибками", example = "1") Long errorAttempts) {
}
