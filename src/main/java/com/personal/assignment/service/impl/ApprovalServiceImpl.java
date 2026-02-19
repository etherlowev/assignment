package com.personal.assignment.service.impl;

import com.personal.assignment.enums.OperationStatus;
import com.personal.assignment.model.Approval;
import com.personal.assignment.model.response.DocumentOpResult;
import com.personal.assignment.repository.ApprovalRepository;
import com.personal.assignment.repository.DocumentRepository;
import com.personal.assignment.service.ApprovalService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;

    private final DocumentRepository documentRepository;

    private final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    public ApprovalServiceImpl(@Autowired ApprovalRepository approvalRepository,
                               @Autowired DocumentRepository documentRepository) {
        this.approvalRepository = approvalRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public Mono<DocumentOpResult> makeEntry(Long documentId) {
        return documentRepository.findById(documentId)
            .flatMap(document -> document == null ?
                null :
                approvalRepository.insertApproval(documentId, LocalDateTime.now())
            )
            .hasElement()
            .map(exists -> exists ?
                new DocumentOpResult(documentId, OperationStatus.SUCCESS) :
                new DocumentOpResult(documentId, OperationStatus.NOT_FOUND)
            );
    }

    @Override
    public Flux<Approval> getApprovals() {
        return approvalRepository.findAll();
    }
}
