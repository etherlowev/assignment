package com.personal.assignment.model.request;

import java.util.Set;

public record BatchDocumentSubmissionBody(Set<Long> documentIds, String initiator) {}
