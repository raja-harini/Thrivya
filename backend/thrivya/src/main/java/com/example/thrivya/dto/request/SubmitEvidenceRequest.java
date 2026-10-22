package com.example.thrivya.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.net.URI;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitEvidenceRequest {

    @NotBlank(message = "Artifact type is required")
    @Pattern(
            regexp = "REPOSITORY_REFERENCE|SOURCE_FILE|PROJECT_REPORT|DEMO_RECORDING|CERTIFICATE|ASSESSMENT_RESULT",
            message = "Unsupported artifact type"
    )
    private String artifactType;

    @Size(
            max = 2048,
            message = "External URL cannot exceed 2048 characters"
    )
    private String externalUrl;

    @NotBlank(message = "Artifact description is required")
    @Size(
            min = 10,
            max = 5000,
            message = "Artifact description must contain between 10 and 5000 characters"
    )
    private String artifactDescription;

    @NotBlank(message = "Your contribution is required")
    @Size(
            min = 10,
            max = 5000,
            message = "Claimed contribution must contain between 10 and 5000 characters"
    )
    private String claimedContribution;

    @Size(
            max = 3000,
            message = "Artifact locator cannot exceed 3000 characters"
    )
    private String artifactLocator;

    @Pattern(
            regexp = "^$|^[a-fA-F0-9]{40}$|^[a-fA-F0-9]{64}$",
            message = "Commit SHA must be a full 40-character or 64-character hexadecimal identifier"
    )
    private String commitSha;

    @Positive(message = "Previous artifact ID must be positive")
    private Long supersedesArtifactId;

    /*
     * This JSON endpoint accepts external references.
     * A separate multipart endpoint will be needed
     * for private file uploads.
     */
    @AssertTrue(message = "A valid HTTPS evidence URL is required")
    public boolean isExternalReferenceValid() {
        if (externalUrl == null || externalUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(externalUrl.trim());

            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && !uri.getHost().isBlank()
                    && uri.getUserInfo() == null;

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @AssertTrue(
            message = "Repository evidence requires a full commit SHA and an artifact locator"
    )
    public boolean isRepositoryReferenceComplete() {
        if (!"REPOSITORY_REFERENCE".equals(artifactType)) {
            return true;
        }

        return commitSha != null
                && !commitSha.isBlank()
                && artifactLocator != null
                && !artifactLocator.isBlank();
    }
}