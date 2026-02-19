package com.personal.assignment.model.response;

import com.personal.assignment.enums.OperationStatus;

public record DocumentOpResult(Long documentId, OperationStatus status) {}
