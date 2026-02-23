package com.personal.assignment.repository;

import com.personal.assignment.model.Approval;
import java.time.ZonedDateTime;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApprovalRepository extends R2dbcRepository<Approval, Long> {
    @Query("INSERT INTO approval (document_id, approval_date) VALUES (:documentId, :approvalDate)")
    Mono<Approval> insertApproval(Long documentId, ZonedDateTime approvalDate);

    Mono<Approval> findByDocumentId(Long documentId);

    Mono<Boolean> existsByDocumentId(Long documentId);
}
