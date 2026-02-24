package com.personal.assignment.model.request.impl;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record BatchDocumentsBody(
    @Schema(description = "Автор пакетной загрузки документов", example = "John") String author,
    @Schema(description = "Список названий документов", example = "[\"Title1\",\"Title2\"]") List<String> titles) {
}
