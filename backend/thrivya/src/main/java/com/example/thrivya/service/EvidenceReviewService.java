package com.example.thrivya.service;

import com.example.thrivya.dto.request.SubmitEvidenceReviewRequest;
import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.thrivya.service.EvidenceAccessService.error;

@Service
@RequiredArgsConstructor
public class EvidenceReviewService {

    private final EvidenceArtifactRepository artifactRepository;
    private final EvidenceReviewRepository reviewRepository;
    private final SkillClaimRepository claimRepository;
    private final EvidenceAccessService access;

    @Transactional(readOnly = true)
    public List<EvidenceDetailResponse> assignedArtifacts(
            String email
    ) {
        User reviewer = access.requireActor(
                email,
                "TECH_INTERVIEWER"
        );

        List<EvidenceDetailResponse> result =
                new ArrayList<>();

        for (Long studentId :
                access.assignedStudentIds(reviewer)) {

            access.requireStudent(studentId);

            for (EvidenceArtifact artifact :
                    artifactRepository
                            .findAllByClaim_User_IdOrderBySubmittedAtDescIdDesc(
                                    studentId
                            )) {

                boolean oldClaim =
                        claimRepository
                                .existsBySupersedesClaim_Id(
                                        artifact.getClaim().getId()
                                );

                boolean oldArtifact =
                        artifactRepository
                                .existsBySupersedesArtifact_Id(
                                        artifact.getId()
                                );

                if (!oldClaim && !oldArtifact) {
                    result.add(
                            access.toEvidenceDetail(
                                    artifact,
                                    true
                            )
                    );
                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public EvidenceDetailResponse getAssignedArtifact(
            String email,
            Long artifactId
    ) {
        User reviewer = access.requireActor(
                email,
                "TECH_INTERVIEWER"
        );

        EvidenceArtifact artifact =
                requireArtifact(artifactId);

        access.requireReviewerAssignment(
                reviewer,
                artifact.getClaim().getUser().getId()
        );

        return access.toEvidenceDetail(artifact, true);
    }

    @Transactional
    public EvidenceDetailResponse submitReview(
            String email,
            Long artifactId,
            SubmitEvidenceReviewRequest request
    ) {
        User reviewer = access.requireActor(
                email,
                "TECH_INTERVIEWER"
        );

        EvidenceArtifact artifact =
                requireArtifact(artifactId);

        access.requireReviewerAssignment(
                reviewer,
                artifact.getClaim().getUser().getId()
        );

        if (artifactRepository
                .existsBySupersedesArtifact_Id(
                        artifactId
                )
                || claimRepository
                .existsBySupersedesClaim_Id(
                        artifact.getClaim().getId()
                )) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Review the current claim and artifact revision"
            );
        }

        EvidenceReview previous = null;

        List<EvidenceReview> ownReviews =
                reviewRepository
                        .findAllByArtifact_IdOrderByReviewedAtDescIdDesc(
                                artifactId
                        )
                        .stream()
                        .filter(review ->
                                review.getReviewer().getId()
                                        .equals(reviewer.getId()))
                        .toList();

        if (request.getSupersedesReviewId() != null) {
            previous = reviewRepository
                    .findByIdAndReviewer_Id(
                            request.getSupersedesReviewId(),
                            reviewer.getId()
                    )
                    .orElseThrow(() ->
                            error(
                                    HttpStatus.NOT_FOUND,
                                    "Previous review not found"
                            ));

            if (!previous.getArtifact().getId()
                    .equals(artifactId)) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Review revisions must concern the same artifact"
                );
            }

            if (ownReviews.isEmpty()
                    || !ownReviews.get(0).getId()
                    .equals(previous.getId())
                    || reviewRepository
                    .existsBySupersedesReview_Id(
                            previous.getId()
                    )) {
                throw error(
                        HttpStatus.CONFLICT,
                        "Only your latest review can be revised"
                );
            }
        } else if (!ownReviews.isEmpty()) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Provide supersedesReviewId to revise your existing review"
            );
        }

        EvidenceReview saved =
                reviewRepository.saveAndFlush(
                        EvidenceReview.builder()
                                .artifact(artifact)
                                .reviewer(reviewer)
                                .outcome(
                                        request.getOutcome()
                                )
                                .supportedScope(
                                        trimNullable(
                                                request.getSupportedScope()
                                        )
                                )
                                .reviewMethod(
                                        request.getReviewMethod().trim()
                                )
                                .reviewNotes(
                                        request.getReviewNotes().trim()
                                )
                                .limitations(
                                        request.getLimitations().trim()
                                )
                                .reviewedArtifactReference(
                                        request.getReviewedArtifactReference().trim()
                                )
                                /*
                                 * No bytes are fetched/captured by this
                                 * external-reference review workflow.
                                 */
                                .reviewedContentSha256(null)
                                .supersedesReview(previous)
                                .build()
                );

        access.audit(
                reviewer,
                "EVIDENCE_REVIEW",
                saved.getId(),
                "REVIEW_ADDED",
                "Approved assigned reviewer recorded a scoped evidence decision."
        );

        return access.toEvidenceDetail(artifact, true);
    }

    private EvidenceArtifact requireArtifact(
            Long artifactId
    ) {
        return artifactRepository.findById(artifactId)
                .orElseThrow(() ->
                        error(
                                HttpStatus.NOT_FOUND,
                                "Artifact not found"
                        ));
    }

    private String trimNullable(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}