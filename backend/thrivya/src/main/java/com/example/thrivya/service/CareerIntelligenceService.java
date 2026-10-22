package com.example.thrivya.service;

import com.example.thrivya.dto.response.CareerIntelligenceProfileResponse;
import com.example.thrivya.dto.response.CareerSkillResponse;
import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.dto.response.ProfileShareResponse;
import com.example.thrivya.model.CareerProfile;
import com.example.thrivya.model.EvidenceArtifact;
import com.example.thrivya.model.EvidenceReview;
import com.example.thrivya.model.EvidenceReviewOutcome;
import com.example.thrivya.model.ProfileShareGrant;
import com.example.thrivya.model.RoleSkill;
import com.example.thrivya.model.SkillClaim;
import com.example.thrivya.model.User;
import com.example.thrivya.repository.CareerProfileRepository;
import com.example.thrivya.repository.EvidenceArtifactRepository;
import com.example.thrivya.repository.RoleSkillRepository;
import com.example.thrivya.repository.SkillClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.thrivya.service.EvidenceAccessService.error;

@Service
@RequiredArgsConstructor
public class CareerIntelligenceService {

    private final CareerProfileRepository profileRepository;
    private final RoleSkillRepository roleSkillRepository;
    private final SkillClaimRepository claimRepository;
    private final EvidenceArtifactRepository artifactRepository;
    private final EvidenceAccessService access;
    private final ProfileShareService shareService;

    @Transactional(readOnly = true)
    public CareerIntelligenceProfileResponse getMyProfile(
            String email
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        return buildProfile(
                student,
                true,
                true,
                null
        );
    }

    @Transactional
    public CareerIntelligenceProfileResponse getRecruiterProfile(
            String email,
            Long studentId
    ) {
        User recruiter = access.requireActor(
                email,
                "RECRUITER"
        );

        ProfileShareGrant grant =
                shareService.requireActiveGrant(
                        recruiter,
                        studentId
                );

        if (!grant.isShareSkillClaims()) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Skill-claim sharing is not permitted"
            );
        }

        User student = access.requireStudent(studentId);

        CareerIntelligenceProfileResponse response =
                buildProfile(
                        student,
                        grant.isShareEvidenceArtifacts(),
                        grant.isShareReviewResults(),
                        access.toShareResponse(grant)
                );

        access.audit(
                recruiter,
                "PROFILE_SHARE_GRANT",
                grant.getId(),
                "RECRUITER_PROFILE_VIEWED",
                "Recruiter viewed the permitted career profile."
        );

        return response;
    }

    @Transactional
    public EvidenceDetailResponse getRecruiterEvidence(
            String email,
            Long studentId,
            Long artifactId
    ) {
        User recruiter = access.requireActor(
                email,
                "RECRUITER"
        );

        ProfileShareGrant grant =
                shareService.requireActiveGrant(
                        recruiter,
                        studentId
                );

        if (!grant.isShareSkillClaims()
                || !grant.isShareEvidenceArtifacts()) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Evidence-artifact sharing is not permitted"
            );
        }

        EvidenceArtifact artifact =
                artifactRepository
                        .findByIdAndClaim_User_Id(
                                artifactId,
                                studentId
                        )
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Artifact not found"
                                ));

        boolean claimWasReplaced =
                claimRepository.existsBySupersedesClaim_Id(
                        artifact.getClaim().getId()
                );

        boolean artifactWasReplaced =
                artifactRepository.existsBySupersedesArtifact_Id(
                        artifactId
                );

        if (claimWasReplaced || artifactWasReplaced) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Artifact is not part of the current shared profile"
            );
        }

        EvidenceDetailResponse response =
                access.toEvidenceDetail(
                        artifact,
                        grant.isShareReviewResults()
                );

        access.audit(
                recruiter,
                "EVIDENCE_ARTIFACT",
                artifactId,
                "RECRUITER_EVIDENCE_VIEWED",
                "Recruiter viewed a permitted evidence reference."
        );

        return response;
    }

    private CareerIntelligenceProfileResponse buildProfile(
            User student,
            boolean includeArtifacts,
            boolean includeReviews,
            ProfileShareResponse sharing
    ) {
        CareerProfile profile =
                profileRepository
                        .findByUserId(student.getId())
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Career profile not found"
                                ));

        List<RoleSkill> requirements =
                profile.getSelectedRole() == null
                        ? List.of()
                        : roleSkillRepository.findAllByRoleId(
                        profile.getSelectedRole().getId()
                );

        Map<Long, RoleSkill> requiredBySkill =
                new LinkedHashMap<>();

        requirements.stream()
                .sorted(
                        Comparator.comparing(
                                RoleSkill::getPriorityLevel,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .forEach(requirement ->
                        requiredBySkill.putIfAbsent(
                                requirement.getSkill().getId(),
                                requirement
                        )
                );

        List<SkillClaim> allClaims =
                claimRepository
                        .findAllByUser_IdOrderByCreatedAtDescIdDesc(
                                student.getId()
                        );

        Set<Long> replacedClaimIds =
                allClaims.stream()
                        .filter(claim ->
                                claim.getSupersedesClaim() != null
                        )
                        .map(claim ->
                                claim.getSupersedesClaim().getId()
                        )
                        .collect(Collectors.toSet());

        List<SkillClaim> currentClaims =
                allClaims.stream()
                        .filter(claim ->
                                !replacedClaimIds.contains(
                                        claim.getId()
                                )
                        )
                        .toList();

        List<EvidenceArtifact> allArtifacts =
                artifactRepository
                        .findAllByClaim_User_IdOrderBySubmittedAtDescIdDesc(
                                student.getId()
                        );

        Set<Long> replacedArtifactIds =
                allArtifacts.stream()
                        .filter(artifact ->
                                artifact.getSupersedesArtifact() != null
                        )
                        .map(artifact ->
                                artifact.getSupersedesArtifact().getId()
                        )
                        .collect(Collectors.toSet());

        Map<Long, List<EvidenceArtifact>> artifactsByClaim =
                allArtifacts.stream()
                        .filter(artifact ->
                                !replacedArtifactIds.contains(
                                        artifact.getId()
                                )
                        )
                        .collect(
                                Collectors.groupingBy(
                                        artifact ->
                                                artifact.getClaim().getId()
                                )
                        );

        List<CareerSkillResponse> strengths =
                new ArrayList<>();

        List<CareerSkillResponse> developing =
                new ArrayList<>();

        List<CareerSkillResponse> additional =
                new ArrayList<>();

        List<CareerSkillResponse> claimCards =
                new ArrayList<>();

        boolean recruiterView = sharing != null;

        for (SkillClaim claim : currentClaims) {
            RoleSkill requirement =
                    requiredBySkill.get(
                            claim.getSkill().getId()
                    );

            List<EvidenceArtifact> claimArtifacts =
                    artifactsByClaim.getOrDefault(
                            claim.getId(),
                            List.of()
                    );

            CareerSkillResponse card =
                    buildClaimCard(
                            claim,
                            claimArtifacts,
                            requirement,
                            includeArtifacts,
                            includeReviews,
                            recruiterView
                    );

            claimCards.add(card);

            if (requirement == null) {
                card.setCategory("ADDITIONAL");
                additional.add(card);

            } else if ("SUPPORTED".equals(
                    card.getEvidenceStatus()
            )) {
                card.setCategory("STRENGTH");
                strengths.add(card);

            } else {
                card.setCategory("DEVELOPING");
                developing.add(card);
            }
        }

        List<CareerSkillResponse> gaps =
                new ArrayList<>();

        int supportedRequirements = 0;
        int partialRequirements = 0;

        for (RoleSkill requirement : requiredBySkill.values()) {
            Long skillId = requirement.getSkill().getId();

            List<CareerSkillResponse> relevant =
                    claimCards.stream()
                            .filter(card ->
                                    skillId.equals(card.getSkillId())
                            )
                            .toList();

            boolean supported =
                    relevant.stream()
                            .anyMatch(card ->
                                    "SUPPORTED".equals(
                                            card.getEvidenceStatus()
                                    )
                            );

            boolean partial =
                    !supported
                            && relevant.stream()
                            .anyMatch(card ->
                                    "PARTIALLY_SUPPORTED".equals(
                                            card.getEvidenceStatus()
                                    )
                            );

            if (supported) {
                supportedRequirements++;
                continue;
            }

            if (partial) {
                partialRequirements++;
            }

            String gapStatus;

            if (!includeReviews) {
                gapStatus = "REVIEW_RESULTS_NOT_SHARED";
            } else if (partial) {
                gapStatus = "PARTIALLY_SUPPORTED";
            } else {
                gapStatus = "NO_SUPPORTED_EVIDENCE";
            }

            gaps.add(
                    CareerSkillResponse.builder()
                            .skillId(skillId)
                            .skillName(
                                    requirement.getSkill()
                                            .getCanonicalName()
                            )
                            .requiredForSelectedRole(true)
                            .rolePriority(
                                    requirement.getPriorityLevel()
                            )
                            .category("ROLE_EVIDENCE_GAP")
                            .evidenceStatus(gapStatus)
                            .evidenceStatusExplanation(
                                    includeReviews
                                            ? "No current claim for this requirement has a fully supported scoped review. This does not prove the skill is absent."
                                            : "Evidence coverage cannot be determined from this sharing scope."
                            )
                            .knowledgeDepthStatus(
                                    "NOT_ASSESSED"
                            )
                            .build()
            );
        }

        /*
         * Do not disclose review-derived gap classifications
         * when review results have not been shared.
         */
        if (!includeReviews) {
            gaps.clear();
        }

        int total = requiredBySkill.size();

        Double coverage = null;

        if (includeReviews && total > 0) {
            coverage =
                    Math.round(
                            supportedRequirements * 10000.0 / total
                    ) / 100.0;
        }

        return CareerIntelligenceProfileResponse.builder()
                .studentId(student.getId())

                /*
                 * Correct getter for User.userName.
                 */
                .studentName(student.getUserName())

                .careerProfileId(profile.getId())
                .selectedRoleId(
                        profile.getSelectedRole() == null
                                ? null
                                : profile.getSelectedRole().getId()
                )
                .selectedRoleTitle(
                        profile.getSelectedRole() == null
                                ? null
                                : profile.getSelectedRole().getTitle()
                )
                .generatedAt(Instant.now())
                .profileBasis(
                        includeReviews
                                ? "Student claims, submitted artifacts, and scoped reviews by currently approved assigned reviewers."
                                : "Student claims; review results are not shared."
                )
                .totalRoleRequirements(total)
                .requirementsWithSupportedEvidence(
                        includeReviews
                                ? supportedRequirements
                                : null
                )
                .requirementsWithPartialEvidence(
                        includeReviews
                                ? partialRequirements
                                : null
                )
                .requirementsWithoutSupportedEvidence(
                        includeReviews
                                ? total - supportedRequirements
                                : null
                )
                .reviewedEvidenceCoveragePercentage(
                        coverage
                )
                .coverageExplanation(
                        "Counts unique role skills with at least one current claim supported within its reviewed scope. Partial requirements are included among requirements without full support. This is not proficiency, employability, or proof of mastery."
                )
                .strengths(strengths)
                .developing(developing)
                .roleEvidenceGaps(gaps)
                .additionalSkills(additional)
                .limitations(
                        List.of(
                                "Reviewed support applies only to the recorded claim scope.",
                                "Knowledge depth has not been assessed by this module.",
                                "Missing reviewed evidence does not establish that a skill is absent.",
                                "Self-reported learning progress is not independent verification.",
                                "ATS scores and extraction confidence are not used as competence proof."
                        )
                )
                .sharing(sharing)
                .build();
    }

    private CareerSkillResponse buildClaimCard(
            SkillClaim claim,
            List<EvidenceArtifact> artifacts,
            RoleSkill requirement,
            boolean includeArtifacts,
            boolean includeReviews,
            boolean recruiterView
    ) {
        List<EvidenceReview> reviews =
                includeReviews
                        ? artifacts.stream()
                        .flatMap(artifact ->
                                access.effectiveReviews(artifact).stream()
                        )
                        .toList()
                        : List.of();

        String status;

        if (!includeReviews || artifacts.isEmpty()) {
            status = "SELF_REPORTED";
        } else {
            status = access.classifyReviews(reviews);
        }

        String supportedScope =
                includeReviews
                        ? reviews.stream()
                        .filter(review ->
                                review.getOutcome()
                                        == EvidenceReviewOutcome.SUPPORTED
                                        || review.getOutcome()
                                        == EvidenceReviewOutcome.PARTIALLY_SUPPORTED
                        )
                        .map(EvidenceReview::getSupportedScope)
                        .filter(Objects::nonNull)
                        .filter(scope -> !scope.isBlank())
                        .distinct()
                        .collect(Collectors.joining("\n"))
                        : null;

        String reviewLimitations =
                includeReviews
                        ? reviews.stream()
                        .map(EvidenceReview::getLimitations)
                        .filter(Objects::nonNull)
                        .filter(limitation -> !limitation.isBlank())
                        .distinct()
                        .collect(Collectors.joining("\n"))
                        : null;

        Instant latestReview =
                reviews.stream()
                        .map(EvidenceReview::getReviewedAt)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .orElse(null);

        long reviewedArtifacts =
                reviews.stream()
                        .map(review ->
                                review.getArtifact().getId()
                        )
                        .distinct()
                        .count();

        /*
         * Count artifacts using the same effective-review
         * classification used in the evidence detail view.
         *
         * This avoids counting an artifact as supported
         * merely because one reviewer supported it while
         * another contradicted it.
         */
        long supportedArtifacts = includeReviews
                ? artifacts.stream()
                .filter(artifact -> {
                    List<EvidenceReview> artifactReviews =
                            reviews.stream()
                                    .filter(review ->
                                            review.getArtifact().getId()
                                                    .equals(artifact.getId())
                                    )
                                    .toList();

                    return "SUPPORTED".equals(
                            access.classifyReviews(
                                    artifactReviews
                            )
                    );
                })
                .count()
                : 0;

        return CareerSkillResponse.builder()
                .skillId(claim.getSkill().getId())
                .skillName(
                        claim.getSkill().getCanonicalName()
                )
                .claimId(claim.getId())
                .claimText(claim.getClaimText())
                .claimSourceType(claim.getSourceType())
                .claimSourceRecordId(
                        recruiterView
                                ? null
                                : claim.getSourceRecordId()
                )
                .claimCreatedAt(claim.getCreatedAt())
                .requiredForSelectedRole(
                        requirement != null
                )
                .rolePriority(
                        requirement == null
                                ? null
                                : requirement.getPriorityLevel()
                )
                .evidenceStatus(status)
                .evidenceStatusExplanation(
                        includeReviews
                                ? "Support is scoped to this claim and its reviewed artifacts."
                                : "Review results are not included; this remains a student claim in this view."
                )
                .knowledgeDepthStatus(
                        "NOT_ASSESSED"
                )
                .knowledgeDepthExplanation(
                        "A recruiter may assess technical depth separately."
                )
                .submittedArtifactCount(
                        includeArtifacts
                                ? artifacts.size()
                                : null
                )
                .reviewedArtifactCount(
                        includeReviews
                                ? (int) reviewedArtifacts
                                : null
                )
                .supportedArtifactCount(
                        includeReviews
                                ? (int) supportedArtifacts
                                : null
                )
                .latestReviewedAt(
                        includeReviews
                                ? latestReview
                                : null
                )
                .supportedScope(supportedScope)
                .limitations(reviewLimitations)
                .artifactIds(
                        includeArtifacts
                                ? artifacts.stream()
                                .map(EvidenceArtifact::getId)
                                .toList()
                                : List.of()
                )
                .reviewIds(
                        includeReviews
                                ? reviews.stream()
                                .map(EvidenceReview::getId)
                                .toList()
                                : List.of()
                )
                .build();
    }
}