package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "evidence_artifacts",
        indexes = {
                @Index(
                        name = "idx_evidence_artifact_claim",
                        columnList = "claim_id"
                ),
                @Index(
                        name = "idx_evidence_artifact_submitted",
                        columnList = "submitted_at"
                ),
                @Index(
                        name = "idx_evidence_artifact_hash",
                        columnList = "content_sha256"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EvidenceArtifact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "claim_id",
            nullable = false,
            updatable = false
    )
    private SkillClaim claim;

    /*
     * Examples:
     * REPOSITORY_REFERENCE
     * SOURCE_FILE
     * PROJECT_REPORT
     * DEMO_RECORDING
     * CERTIFICATE
     * ASSESSMENT_RESULT
     */
    @Column(
            name = "artifact_type",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String artifactType;

    /*
     * Optional external reference.
     *
     * Storing a URL does not mean Thrivya has checked
     * the URL, ownership, content, or authenticity.
     */
    @Column(
            name = "external_url",
            updatable = false,
            length = 2048
    )
    private String externalUrl;

    /*
     * Optional private storage key for a file stored
     * by the backend.
     *
     * Do not store a publicly accessible download URL
     * here for private evidence.
     */
    @Column(
            name = "storage_key",
            updatable = false,
            length = 1024
    )
    private String storageKey;

    @Column(
            name = "artifact_description",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String artifactDescription;

    @Column(
            name = "claimed_contribution",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String claimedContribution;

    /*
     * Example:
     * src/main/java/.../JwtAuthenticationFilter.java
     *
     * Helps the reviewer find the relevant material.
     */
    @Column(
            name = "artifact_locator",
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String artifactLocator;

    /*
     * Optional Git commit identifier.
     *
     * This is a submitted reference until the backend
     * or reviewer checks it.
     */
    @Column(
            name = "commit_sha",
            updatable = false,
            length = 64
    )
    private String commitSha;

    /*
     * SHA-256 of actual bytes captured by the backend.
     *
     * Leave null when no artifact bytes were captured.
     * Do not hash only a URL and call it a content hash.
     */
    @Column(
            name = "content_sha256",
            updatable = false,
            length = 64
    )
    private String contentSha256;

    @Column(
            name = "content_captured_at",
            updatable = false
    )
    private Instant contentCapturedAt;

    /*
     * New evidence versions should be inserted as
     * new records instead of replacing reviewed content.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "supersedes_artifact_id",
            updatable = false
    )
    private EvidenceArtifact supersedesArtifact;

    @Column(
            name = "submitted_at",
            nullable = false,
            updatable = false
    )
    private Instant submittedAt;

    @PrePersist
    protected void beforeCreate() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }
}