package com.personal.assignment.model.request.impl;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record BatchDocumentSubmissionBody(
    @Schema(description = "Список идентификаторов документов", example = "[\"1\",\"2\",\"3\"]") Set<Long> documentIds,
    @Schema(description = "Инициатор операции", example = "John") String initiator) {}
