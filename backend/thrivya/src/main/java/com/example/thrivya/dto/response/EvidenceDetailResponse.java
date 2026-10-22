package com.example.thrivya.dto.response;

import com.example.thrivya.model.EvidenceReviewOutcome;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceDetailResponse {

    private Long artifactId;

    private Long claimId;

    private Long skillId;

    private String skillName;

    private String claimText;

    private String claimSourceType;

    private Instant claimCreatedAt;

    private String artifactType;

    private String artifactDescription;

    private String claimedContribution;

    /*
     * Student-provided external reference.
     * Not an automatic authenticity or ownership guarantee.
     */
    private String externalUrl;

    /*
     * Optional backend-generated authorized download route.
     *
     * Do not expose the internal storageKey or server path.
     */
    private String downloadUrl;

    private String artifactLocator;

    private String commitSha;

    private String contentSha256;

    private Instant contentCapturedAt;

    private Instant submittedAt;

    private Long supersedesArtifactId;

    /*
     * Examples:
     * SUBMITTED
     * SUPPORTED
     * PARTIALLY_SUPPORTED
     * NEEDS_REVIEW
     *
     * The service must apply its review-selection policy.
     */
    private String evidenceStatus;

    private String evidenceStatusExplanation;

    private String hashExplanation;

    /*
     * Used when the recruiter may see the artifact
     * but not the review details.
     */
    private boolean reviewResultsVisible;

    @Builder.Default
    private List<ReviewResponse> reviews =
            new ArrayList<>();

    @Builder.Default
    private List<String> limitations =
            new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {

        private Long reviewId;

        private Long reviewerId;

        private String reviewerDisplayName;

        private EvidenceReviewOutcome outcome;

        private String supportedScope;

        private String reviewMethod;

        private String reviewNotes;

        private String limitations;

        private String reviewedArtifactReference;

        private String reviewedContentSha256;

        private Instant reviewedAt;

        private Long supersedesReviewId;

        /*
         * True when a later review explicitly replaces
         * this review according to service policy.
         */
        private boolean superseded;
    }
}