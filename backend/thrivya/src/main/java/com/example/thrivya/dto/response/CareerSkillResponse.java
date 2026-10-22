package com.example.thrivya.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerSkillResponse {

    private Long skillId;

    private String skillName;

    /*
     * Null when representing a role requirement with
     * no submitted claim.
     */
    private Long claimId;

    private String claimText;

    private String claimSourceType;

    private Long claimSourceRecordId;

    private Instant claimCreatedAt;

    private boolean requiredForSelectedRole;

    private Integer rolePriority;

    /*
     * Suggested display groups:
     * STRENGTH
     * DEVELOPING
     * ROLE_EVIDENCE_GAP
     * ADDITIONAL
     */
    private String category;

    /*
     * Examples:
     * SELF_REPORTED
     * SUBMITTED
     * SUPPORTED
     * PARTIALLY_SUPPORTED
     * INSUFFICIENT
     * CONTRADICTED
     * NEEDS_REVIEW
     * NO_EVIDENCE
     */
    private String evidenceStatus;

    /*
     * Example:
     * Supported within the scope of the reviewed project.
     */
    private String evidenceStatusExplanation;

    /*
     * Default must remain NOT_ASSESSED unless a
     * separate authorized assessment establishes otherwise.
     */
    @Builder.Default
    private String knowledgeDepthStatus = "NOT_ASSESSED";

    private String knowledgeDepthExplanation;

    private Integer submittedArtifactCount;

    private Integer reviewedArtifactCount;

    private Integer supportedArtifactCount;

    private Instant latestReviewedAt;

    private String supportedScope;

    private String limitations;

    @Builder.Default
    private List<Long> artifactIds =
            new ArrayList<>();

    @Builder.Default
    private List<Long> reviewIds =
            new ArrayList<>();
}