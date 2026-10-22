package com.example.thrivya.repository;

import com.example.thrivya.model.EvidenceAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EvidenceAuditEventRepository
        extends JpaRepository<EvidenceAuditEvent, Long> {

    Page<EvidenceAuditEvent> findAllByActor_IdOrderByOccurredAtDescIdDesc(
            Long actorId,
            Pageable pageable
    );

    Page<EvidenceAuditEvent> findAllByEntityTypeAndEntityIdOrderByOccurredAtDescIdDesc(
            String entityType,
            Long entityId,
            Pageable pageable
    );

    Page<EvidenceAuditEvent> findAllByActionOrderByOccurredAtDescIdDesc(
            String action,
            Pageable pageable
    );

    List<EvidenceAuditEvent> findAllByRequestIdOrderByOccurredAtAscIdAsc(
            String requestId
    );

    Page<EvidenceAuditEvent> findAllByOccurredAtBetweenOrderByOccurredAtDescIdDesc(
            Instant from,
            Instant to,
            Pageable pageable
    );
}