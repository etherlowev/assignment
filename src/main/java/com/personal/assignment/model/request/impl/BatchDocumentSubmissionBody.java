package com.personal.assignment.model.request.impl;

import java.util.Set;

public record BatchDocumentSubmissionBody(Set<Long> documentIds, String initiator) {}
