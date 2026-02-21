package com.personal.assignment.model.request.impl;

public record ParallelApproveRequestBody(Long documentId,
                                         String initiator,
                                         Integer threads,
                                         Integer attempts) {}
