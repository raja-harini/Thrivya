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
public class CareerIntelligenceProfileResponse {

    private Long studentId;

    private String studentName;

    private Long careerProfileId;

    private Long selectedRoleId;

    private String selectedRoleTitle;

    private Instant generatedAt;

    /*
     * Example:
     * Based on student submissions and scoped reviewer checks.
     */
    private String profileBasis;

    /*
     * These counts refer to unique requirements
     * for the selected career role.
     */
    private Integer totalRoleRequirements;

    private Integer requirementsWithSupportedEvidence;

    private Integer requirementsWithPartialEvidence;

    private Integer requirementsWithoutSupportedEvidence;

    /*
     * Evidence coverage, not proficiency or employability.
     *
     * Return null when no selected-role requirements
     * are configured.
     */
    private Double reviewedEvidenceCoveragePercentage;

    private String coverageExplanation;

    @Builder.Default
    private List<CareerSkillResponse> strengths =
            new ArrayList<>();

    @Builder.Default
    private List<CareerSkillResponse> developing =
            new ArrayList<>();

    @Builder.Default
    private List<CareerSkillResponse> roleEvidenceGaps =
            new ArrayList<>();

    /*
     * Supporting claims outside the selected role.
     * Useful in the student's own profile.
     */
    @Builder.Default
    private List<CareerSkillResponse> additionalSkills =
            new ArrayList<>();

    @Builder.Default
    private List<String> limitations =
            new ArrayList<>();

    /*
     * Null for the student's own view.
     * Recruiter responses may include their applicable grant.
     */
    private ProfileShareResponse sharing;
}