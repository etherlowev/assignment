package com.personal.assignment.model.response;

public record ParallelApproveResponse(Long total,
                                      Long successfulAttempts,
                                      Long conflictedAttempts,
                                      Long errorAttempts) {
}
