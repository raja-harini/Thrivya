package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "skill_claims",
        indexes = {
                @Index(
                        name = "idx_skill_claim_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_skill_claim_user_skill",
                        columnList = "user_id, skill_id"
                ),
                @Index(
                        name = "idx_skill_claim_source",
                        columnList = "source_type, source_record_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SkillClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "skill_id",
            nullable = false,
            updatable = false
    )
    private Skill skill;

    @Column(
            name = "claim_text",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String claimText;

    /*
     * Examples:
     * RESUME_TEXT
     * MANUAL_INPUT
     * MANUAL_PROGRESS
     * STUDENT_PROJECT
     */
    @Column(
            name = "source_type",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String sourceType;

    /*
     * ID of the source record in its corresponding table.
     *
     * Examples:
     * Resume ID
     * SkillProgressUpdate ID
     * StudentProject ID
     *
     * This is a provenance reference, not a database
     * foreign key because the source can be in different tables.
     */
    @Column(
            name = "source_record_id",
            updatable = false
    )
    private Long sourceRecordId;

    /*
     * If the student changes the claim, create a new claim
     * referencing the previous one.
     *
     * Previous evidence and reviews remain attached to
     * the previous claim.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "supersedes_claim_id",
            updatable = false
    )
    private SkillClaim supersedesClaim;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void beforeCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}