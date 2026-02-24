package com.personal.assignment.model.request.impl;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentCreationBody(
    @Schema(description = "Автор документа", example = "Марк") String author,
    @Schema(description = "Название документа", example = "Регламент") String title) {
}
