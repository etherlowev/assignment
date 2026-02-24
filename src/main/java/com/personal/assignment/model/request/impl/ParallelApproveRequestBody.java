package com.personal.assignment.model.request.impl;

import io.swagger.v3.oas.annotations.media.Schema;

public record ParallelApproveRequestBody(
    @Schema(description = "Идентификатор документа", example = "1") Long documentId,
    @Schema(description = "Инициатор операции", example = "Василий") String initiator,
    @Schema(description = "Количество потоков", example = "4") Integer threads,
    @Schema(description = "Количество потоков в потоке", example = "20") Integer attempts) {}
