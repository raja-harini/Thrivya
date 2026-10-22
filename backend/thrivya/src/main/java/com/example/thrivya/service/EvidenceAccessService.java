package com.example.thrivya.service;

import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.dto.response.ProfileShareResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.EvidenceAuditEventRepository;
import com.example.thrivya.repository.EvidenceReviewRepository;
import com.example.thrivya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvidenceAccessService {

    private final UserRepository userRepository;
    private final EvidenceReviewRepository reviewRepository;
    private final EvidenceAuditEventRepository auditRepository;

    @Value("${thrivya.evidence.approved-reviewer-ids:}")
    private String approvedReviewerIds;

    @Value("${thrivya.evidence.approved-recruiter-ids:}")
    private String approvedRecruiterIds;

    @Value("${thrivya.evidence.reviewer-student-assignments:}")
    private String reviewerStudentAssignments;

    public User requireActor(
            String email,
            String role
    ) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || email == null
                || !email.equals(authentication.getName())) {
            throw error(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.UNAUTHORIZED,
                                        "Authenticated account not found"
                                ));

        boolean authorityMatches =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                ("ROLE_" + role).equals(
                                        authority.getAuthority()
                                )
                        );

        if (!authorityMatches
                || !hasAccountRole(user, role)) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "This account cannot perform this action"
            );
        }

        return user;
    }

    public User requireStudent(
            Long studentId
    ) {
        if (studentId == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Student ID is required"
            );
        }

        User student =
                userRepository.findById(studentId)
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Student not found"
                                ));

        if (!hasAccountRole(student, "STUDENT")) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Student not found"
            );
        }

        return student;
    }

    public User requireApprovedRecruiter(
            Long recruiterId
    ) {
        if (recruiterId == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Recruiter ID is required"
            );
        }

        User recruiter =
                userRepository.findById(recruiterId)
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Recruiter not found"
                                ));

        if (!hasAccountRole(recruiter, "RECRUITER")
                || !containsId(
                approvedRecruiterIds,
                recruiterId
        )) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Recruiter account has not been approved"
            );
        }

        return recruiter;
    }

    public void requireReviewerAssignment(
            User reviewer,
            Long studentId
    ) {
        if (reviewer == null
                || reviewer.getId() == null
                || !hasAccountRole(
                reviewer,
                "TECH_INTERVIEWER"
        )) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Technical interviewer account required"
            );
        }

        if (studentId == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Student ID is required"
            );
        }

        if (reviewer.getId().equals(studentId)) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Self-review is not allowed"
            );
        }

        if (!isReviewerApprovedAndAssigned(
                reviewer.getId(),
                studentId
        )) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Reviewer is not approved and assigned to this student"
            );
        }
    }

    public Set<Long> assignedStudentIds(
            User reviewer
    ) {
        if (reviewer == null
                || reviewer.getId() == null
                || !hasAccountRole(
                reviewer,
                "TECH_INTERVIEWER"
        )
                || !containsId(
                approvedReviewerIds,
                reviewer.getId()
        )) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "Reviewer account has not been approved"
            );
        }

        Set<Long> result = new LinkedHashSet<>();

        for (String assignment :
                tokens(reviewerStudentAssignments)) {

            String[] parts =
                    assignment.split(":", -1);

            if (parts.length != 2) {
                throw error(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Invalid reviewer assignment configuration"
                );
            }

            String configuredReviewerId =
                    parts[0].trim();

            String configuredStudentId =
                    parts[1].trim();

            try {
                long parsedReviewerId =
                        Long.parseLong(
                                configuredReviewerId
                        );

                long parsedStudentId =
                        Long.parseLong(
                                configuredStudentId
                        );

                if (parsedReviewerId <= 0
                        || parsedStudentId <= 0) {
                    throw new NumberFormatException(
                            "IDs must be positive"
                    );
                }

                if (parsedReviewerId
                        == reviewer.getId().longValue()) {
                    result.add(parsedStudentId);
                }

            } catch (NumberFormatException exception) {
                throw error(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Invalid reviewer assignment configuration"
                );
            }
        }

        result.remove(reviewer.getId());

        return result;
    }

    public boolean isReviewerApprovedAndAssigned(
            Long reviewerId,
            Long studentId
    ) {
        if (reviewerId == null
                || studentId == null
                || reviewerId.equals(studentId)) {
            return false;
        }

        if (!containsId(
                approvedReviewerIds,
                reviewerId
        )) {
            return false;
        }

        for (String assignment :
                tokens(reviewerStudentAssignments)) {

            String[] parts =
                    assignment.split(":", -1);

            if (parts.length == 2
                    && parts[0].trim().equals(
                    reviewerId.toString()
            )
                    && parts[1].trim().equals(
                    studentId.toString()
            )) {
                return true;
            }
        }

        return false;
    }

    /*
     * Only currently approved and assigned reviewers
     * contribute to the current evidence classification.
     *
     * The most recent review per reviewer is used.
     */
    public List<EvidenceReview> effectiveReviews(
            EvidenceArtifact artifact
    ) {
        Long studentId =
                artifact.getClaim()
                        .getUser()
                        .getId();

        Map<Long, EvidenceReview> latestByReviewer =
                new LinkedHashMap<>();

        List<EvidenceReview> storedReviews =
                reviewRepository
                        .findAllByArtifact_IdOrderByReviewedAtDescIdDesc(
                                artifact.getId()
                        );

        for (EvidenceReview review : storedReviews) {
            User reviewer = review.getReviewer();

            Long reviewerId = reviewer.getId();

            if (hasAccountRole(
                    reviewer,
                    "TECH_INTERVIEWER"
            ) && isReviewerApprovedAndAssigned(
                    reviewerId,
                    studentId
            )) {
                latestByReviewer.putIfAbsent(
                        reviewerId,
                        review
                );
            }
        }

        return new ArrayList<>(
                latestByReviewer.values()
        );
    }

    public String classifyReviews(
            List<EvidenceReview> reviews
    ) {
        if (reviews == null || reviews.isEmpty()) {
            return "SUBMITTED";
        }

        Set<EvidenceReviewOutcome> outcomes =
                reviews.stream()
                        .map(EvidenceReview::getOutcome)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        if (outcomes.isEmpty()) {
            return "SUBMITTED";
        }

        boolean positive =
                outcomes.contains(
                        EvidenceReviewOutcome.SUPPORTED
                )
                        || outcomes.contains(
                        EvidenceReviewOutcome.PARTIALLY_SUPPORTED
                );

        if (outcomes.contains(
                EvidenceReviewOutcome.CONTRADICTED
        )) {
            return positive
                    ? "NEEDS_REVIEW"
                    : "CONTRADICTED";
        }

        if (outcomes.contains(
                EvidenceReviewOutcome.NEEDS_REVIEW
        )) {
            return "NEEDS_REVIEW";
        }

        if (outcomes.contains(
                EvidenceReviewOutcome.SUPPORTED
        )) {
            return "SUPPORTED";
        }

        if (outcomes.contains(
                EvidenceReviewOutcome.PARTIALLY_SUPPORTED
        )) {
            return "PARTIALLY_SUPPORTED";
        }

        return "INSUFFICIENT";
    }

    public EvidenceDetailResponse toEvidenceDetail(
            EvidenceArtifact artifact,
            boolean includeReviews
    ) {
        SkillClaim claim = artifact.getClaim();

        List<EvidenceReview> effective =
                includeReviews
                        ? effectiveReviews(artifact)
                        : List.of();

        List<EvidenceDetailResponse.ReviewResponse>
                reviews =
                effective.stream()
                        .map(review ->
                                EvidenceDetailResponse
                                        .ReviewResponse
                                        .builder()
                                        .reviewId(
                                                review.getId()
                                        )
                                        .reviewerId(
                                                review.getReviewer()
                                                        .getId()
                                        )
                                        .reviewerDisplayName(
                                                review.getReviewer()
                                                        .getUserName()
                                        )
                                        .outcome(
                                                review.getOutcome()
                                        )
                                        .supportedScope(
                                                review.getSupportedScope()
                                        )
                                        .reviewMethod(
                                                review.getReviewMethod()
                                        )
                                        .reviewNotes(
                                                review.getReviewNotes()
                                        )
                                        .limitations(
                                                review.getLimitations()
                                        )
                                        .reviewedArtifactReference(
                                                review.getReviewedArtifactReference()
                                        )
                                        .reviewedContentSha256(
                                                review.getReviewedContentSha256()
                                        )
                                        .reviewedAt(
                                                review.getReviewedAt()
                                        )
                                        .supersedesReviewId(
                                                review.getSupersedesReview() == null
                                                        ? null
                                                        : review.getSupersedesReview()
                                                        .getId()
                                        )
                                        .superseded(false)
                                        .build()
                        )
                        .toList();

        return EvidenceDetailResponse.builder()
                .artifactId(
                        artifact.getId()
                )
                .claimId(
                        claim.getId()
                )
                .skillId(
                        claim.getSkill()
                                .getId()
                )
                .skillName(
                        claim.getSkill()
                                .getCanonicalName()
                )
                .claimText(
                        claim.getClaimText()
                )
                .claimSourceType(
                        claim.getSourceType()
                )
                .claimCreatedAt(
                        claim.getCreatedAt()
                )
                .artifactType(
                        artifact.getArtifactType()
                )
                .artifactDescription(
                        artifact.getArtifactDescription()
                )
                .claimedContribution(
                        artifact.getClaimedContribution()
                )
                .externalUrl(
                        artifact.getExternalUrl()
                )
                .downloadUrl(null)
                .artifactLocator(
                        artifact.getArtifactLocator()
                )
                .commitSha(
                        artifact.getCommitSha()
                )
                .contentSha256(
                        artifact.getContentSha256()
                )
                .contentCapturedAt(
                        artifact.getContentCapturedAt()
                )
                .submittedAt(
                        artifact.getSubmittedAt()
                )
                .supersedesArtifactId(
                        artifact.getSupersedesArtifact() == null
                                ? null
                                : artifact.getSupersedesArtifact()
                                .getId()
                )
                .evidenceStatus(
                        includeReviews
                                ? classifyReviews(effective)
                                : "SUBMITTED"
                )
                .evidenceStatusExplanation(
                        includeReviews
                                ? "Based on current approved reviewer decisions; support is limited to the recorded scope."
                                : "Review results are not included in this view."
                )
                .hashExplanation(
                        "A content hash identifies captured bytes. It does not establish authorship or skill proficiency."
                )
                .reviewResultsVisible(
                        includeReviews
                )
                .reviews(
                        reviews
                )
                .limitations(
                        List.of(
                                "A submitted URL is not independent verification.",
                                "Knowledge depth is not established by this evidence record.",
                                "External resources can change; inspect the recorded version reference."
                        )
                )
                .build();
    }

    public ProfileShareResponse toShareResponse(
            ProfileShareGrant grant
    ) {
        Instant now = Instant.now();

        String status;

        if (grant.getRevokedAt() != null) {
            status = "REVOKED";

        } else if (!grant.getExpiresAt()
                .isAfter(now)) {
            status = "EXPIRED";

        } else if (grant.getCreatedAt()
                .isAfter(now)) {
            status = "NOT_YET_ACTIVE";

        } else {
            status = "ACTIVE";
        }

        return ProfileShareResponse.builder()
                .shareId(
                        grant.getId()
                )
                .studentId(
                        grant.getStudent()
                                .getId()
                )
                .recruiterId(
                        grant.getRecruiter()
                                .getId()
                )
                .recruiterDisplayName(
                        grant.getRecruiter()
                                .getUserName()
                )
                .shareSkillClaims(
                        grant.isShareSkillClaims()
                )
                .shareEvidenceArtifacts(
                        grant.isShareEvidenceArtifacts()
                )
                .shareReviewResults(
                        grant.isShareReviewResults()
                )
                .createdAt(
                        grant.getCreatedAt()
                )
                .expiresAt(
                        grant.getExpiresAt()
                )
                .revokedAt(
                        grant.getRevokedAt()
                )
                .active(
                        grant.isActiveAt(now)
                )
                .status(
                        status
                )
                .message(
                        "Sharing applies to the current permitted career profile."
                )
                .build();
    }

    public void audit(
            User actor,
            String entityType,
            Long entityId,
            String action,
            String details
    ) {
        auditRepository.save(
                EvidenceAuditEvent.builder()
                        .actor(actor)
                        .entityType(entityType)
                        .entityId(entityId)
                        .action(action)
                        .details(details)
                        .build()
        );
    }

    public static ResponseStatusException error(
            HttpStatus status,
            String message
    ) {
        return new ResponseStatusException(
                status,
                message
        );
    }

    private boolean hasAccountRole(
            User user,
            String expectedRole
    ) {
        return user != null
                && user.getAccountRole() != null
                && expectedRole != null
                && expectedRole.equals(
                user.getAccountRole().name()
        );
    }

    private boolean containsId(
            String configuration,
            Long id
    ) {
        return id != null
                && tokens(configuration)
                .contains(id.toString());
    }

    private Set<String> tokens(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(
                        value.split(",")
                )
                .map(String::trim)
                .filter(token ->
                        !token.isBlank()
                )
                .collect(Collectors.toSet());
    }
}