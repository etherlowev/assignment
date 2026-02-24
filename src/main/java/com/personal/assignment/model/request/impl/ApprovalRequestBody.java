package com.personal.assignment.model.request.impl;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApprovalRequestBody(@Schema(description = "Идентификатор документа", example = "1") Long documentId) {}
