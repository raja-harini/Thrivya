package com.example.thrivya.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSkillClaimRequest {

    @NotNull(message = "Skill ID is required")
    @Positive(message = "Skill ID must be positive")
    private Long skillId;

    @NotBlank(message = "Claim text is required")
    @Size(
            min = 10,
            max = 3000,
            message = "Claim text must contain between 10 and 3000 characters"
    )
    private String claimText;

    @NotBlank(message = "Source type is required")
    @Pattern(
            regexp = "RESUME_TEXT|MANUAL_INPUT|MANUAL_PROGRESS|STUDENT_PROJECT",
            message = "Source type must be RESUME_TEXT, MANUAL_INPUT, MANUAL_PROGRESS, or STUDENT_PROJECT"
    )
    private String sourceType;

    @Positive(message = "Source record ID must be positive")
    private Long sourceRecordId;

    @Positive(message = "Previous claim ID must be positive")
    private Long supersedesClaimId;

    @AssertTrue(
            message = "Source record ID is required for resume, progress, and project claims"
    )
    public boolean isSourceReferenceValid() {
        if (sourceType == null) {
            return true;
        }

        return switch (sourceType) {
            case "RESUME_TEXT",
                 "MANUAL_PROGRESS",
                 "STUDENT_PROJECT" -> sourceRecordId != null;
            default -> true;
        };
    }
}