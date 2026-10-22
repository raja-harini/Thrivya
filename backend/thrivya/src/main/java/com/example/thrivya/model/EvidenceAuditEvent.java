package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "evidence_audit_events",
        indexes = {
                @Index(
                        name = "idx_evidence_audit_actor",
                        columnList = "actor_id"
                ),
                @Index(
                        name = "idx_evidence_audit_entity",
                        columnList = "entity_type, entity_id"
                ),
                @Index(
                        name = "idx_evidence_audit_time",
                        columnList = "occurred_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvidenceAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Nullable for backend/system-generated events.
     *
     * For user actions, set this from the authenticated
     * user on the backend.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actor_id",
            updatable = false
    )
    private User actor;

    /*
     * Examples:
     * SKILL_CLAIM
     * EVIDENCE_ARTIFACT
     * EVIDENCE_REVIEW
     * PROFILE_SHARE_GRANT
     */
    @Column(
            name = "entity_type",
            nullable = false,
            updatable = false,
            length = 60
    )
    private String entityType;

    @Column(
            name = "entity_id",
            nullable = false,
            updatable = false
    )
    private Long entityId;

    /*
     * Examples:
     * CLAIM_CREATED
     * CLAIM_REVISED
     * EVIDENCE_SUBMITTED
     * REVIEW_ADDED
     * PROFILE_SHARED
     * SHARE_REVOKED
     * RECRUITER_PROFILE_VIEWED
     */
    @Column(
            name = "action",
            nullable = false,
            updatable = false,
            length = 80
    )
    private String action;

    /*
     * Human-readable event summary.
     *
     * Do not store passwords, JWTs, private keys,
     * or complete sensitive documents here.
     */
    @Column(
            name = "details",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String details;

    /*
     * Optional request/correlation identifier for
     * connecting application logs with this event.
     */
    @Column(
            name = "request_id",
            updatable = false,
            length = 100
    )
    private String requestId;

    @Column(
            name = "occurred_at",
            nullable = false,
            updatable = false
    )
    private Instant occurredAt;

    @PrePersist
    protected void beforeCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}