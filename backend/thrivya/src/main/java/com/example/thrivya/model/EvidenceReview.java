package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "evidence_reviews",
        indexes = {
                @Index(
                        name = "idx_evidence_review_artifact",
                        columnList = "artifact_id"
                ),
                @Index(
                        name = "idx_evidence_review_reviewer",
                        columnList = "reviewer_id"
                ),
                @Index(
                        name = "idx_evidence_review_date",
                        columnList = "reviewed_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvidenceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "artifact_id",
            nullable = false,
            updatable = false
    )
    private EvidenceArtifact artifact;

    /*
     * Set by the backend from the authenticated,
     * approved reviewer account.
     *
     * Never accept reviewer identity from a student.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false,
            updatable = false
    )
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "outcome",
            nullable = false,
            updatable = false,
            length = 40
    )
    private EvidenceReviewOutcome outcome;

    /*
     * Exact part of the claim supported by the review.
     *
     * For an insufficient/contradicted review, this may
     * be null or describe only the supported portion.
     */
    @Column(
            name = "supported_scope",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String supportedScope;

    /*
     * Example:
     * Inspected referenced source files and observed
     * the student demonstrate the application.
     */
    @Column(
            name = "review_method",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String reviewMethod;

    @Column(
            name = "review_notes",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String reviewNotes;

    /*
     * Must explain what was not established.
     *
     * Example:
     * Production scalability and deployment security
     * were not assessed.
     */
    @Column(
            name = "limitations",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String limitations;

    /*
     * Human-readable description of the version reviewed.
     *
     * Example:
     * Repository snapshot at commit abc123;
     * AuthController.java and SecurityConfig.java.
     */
    @Column(
            name = "reviewed_artifact_reference",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String reviewedArtifactReference;

    /*
     * Optional hash of the actual content reviewed.
     */
    @Column(
            name = "reviewed_content_sha256",
            updatable = false,
            length = 64
    )
    private String reviewedContentSha256;

    /*
     * A correction or reassessment is a new review,
     * linked to the earlier review.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "supersedes_review_id",
            updatable = false
    )
    private EvidenceReview supersedesReview;

    @Column(
            name = "reviewed_at",
            nullable = false,
            updatable = false
    )
    private Instant reviewedAt;

    @PrePersist
    protected void beforeCreate() {
        if (reviewedAt == null) {
            reviewedAt = Instant.now();
        }
    }
}