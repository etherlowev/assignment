package com.personal.assignment.model.response;

import com.personal.assignment.model.Document;
import com.personal.assignment.model.History;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DocumentWithHistory(
    @Schema(description = "Документ") Document document,
    @Schema(description = "История") List<History> history) {}
