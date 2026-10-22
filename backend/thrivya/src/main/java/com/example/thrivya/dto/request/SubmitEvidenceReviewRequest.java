package com.example.thrivya.dto.request;

import com.example.thrivya.model.EvidenceReviewOutcome;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitEvidenceReviewRequest {

    @NotNull(message = "Review outcome is required")
    private EvidenceReviewOutcome outcome;

    @Size(
            max = 5000,
            message = "Supported scope cannot exceed 5000 characters"
    )
    private String supportedScope;

    @NotBlank(message = "Review method is required")
    @Size(
            min = 10,
            max = 5000,
            message = "Review method must contain between 10 and 5000 characters"
    )
    private String reviewMethod;

    @NotBlank(message = "Review notes are required")
    @Size(
            min = 10,
            max = 10000,
            message = "Review notes must contain between 10 and 10000 characters"
    )
    private String reviewNotes;

    @NotBlank(message = "Review limitations are required")
    @Size(
            min = 10,
            max = 5000,
            message = "Limitations must contain between 10 and 5000 characters"
    )
    private String limitations;

    @NotBlank(message = "Reviewed artifact reference is required")
    @Size(
            min = 10,
            max = 5000,
            message = "Reviewed artifact reference must contain between 10 and 5000 characters"
    )
    private String reviewedArtifactReference;

    @Positive(message = "Previous review ID must be positive")
    private Long supersedesReviewId;

    @AssertTrue(
            message = "Supported scope is required for SUPPORTED or PARTIALLY_SUPPORTED outcomes"
    )
    public boolean isSupportedScopeProvided() {
        if (outcome == null) {
            return true;
        }

        boolean requiresScope =
                outcome == EvidenceReviewOutcome.SUPPORTED
                        || outcome == EvidenceReviewOutcome.PARTIALLY_SUPPORTED;

        return !requiresScope
                || (supportedScope != null
                && supportedScope.trim().length() >= 10);
    }
}