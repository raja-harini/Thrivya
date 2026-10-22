package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "profile_share_grants",
        indexes = {
                @Index(
                        name = "idx_profile_share_student",
                        columnList = "student_id"
                ),
                @Index(
                        name = "idx_profile_share_recruiter",
                        columnList = "recruiter_id"
                ),
                @Index(
                        name = "idx_profile_share_pair",
                        columnList = "student_id, recruiter_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProfileShareGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false,
            updatable = false
    )
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "recruiter_id",
            nullable = false,
            updatable = false
    )
    private User recruiter;

    /*
     * Explicit sharing scope.
     *
     * Services must filter DTO fields according to these
     * permissions before returning recruiter responses.
     */
    @Builder.Default
    @Column(
            name = "share_skill_claims",
            nullable = false,
            updatable = false
    )
    private boolean shareSkillClaims = false;

    @Builder.Default
    @Column(
            name = "share_evidence_artifacts",
            nullable = false,
            updatable = false
    )
    private boolean shareEvidenceArtifacts = false;

    @Builder.Default
    @Column(
            name = "share_review_results",
            nullable = false,
            updatable = false
    )
    private boolean shareReviewResults = false;

    @Column(
            name = "expires_at",
            nullable = false,
            updatable = false
    )
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    /*
     * Optimistic locking for concurrent updates,
     * especially revocation.
     *
     * Do not assign this field manually.
     */
    @Version
    @Column(name = "lock_version")
    private Long lockVersion;

    @PrePersist
    protected void beforeCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (expiresAt == null
                || !expiresAt.isAfter(createdAt)) {
            throw new IllegalStateException(
                    "Sharing expiry must be after creation time"
            );
        }
    }

    public boolean isActiveAt(Instant time) {
        return time != null
                && createdAt != null
                && !time.isBefore(createdAt)
                && revokedAt == null
                && expiresAt != null
                && time.isBefore(expiresAt);
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }
}