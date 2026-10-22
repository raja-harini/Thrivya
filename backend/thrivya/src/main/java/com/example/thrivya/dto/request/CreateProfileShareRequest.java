package com.example.thrivya.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfileShareRequest {

    @NotNull(message = "Recruiter ID is required")
    @Positive(message = "Recruiter ID must be positive")
    private Long recruiterId;

    @NotNull(message = "Sharing expiry is required")
    @Future(message = "Sharing expiry must be in the future")
    private Instant expiresAt;

    @Builder.Default
    private boolean shareSkillClaims = false;

    @Builder.Default
    private boolean shareEvidenceArtifacts = false;

    @Builder.Default
    private boolean shareReviewResults = false;

    @AssertTrue(
            message = "At least one sharing permission must be selected"
    )
    public boolean isAnyPermissionSelected() {
        return shareSkillClaims
                || shareEvidenceArtifacts
                || shareReviewResults;
    }

    @AssertTrue(
            message = "Sharing evidence or review results also requires sharing the related skill claims"
    )
    public boolean isSharingScopeConsistent() {
        return shareSkillClaims
                || (!shareEvidenceArtifacts && !shareReviewResults);
    }
}