package com.example.thrivya.repository;

import com.example.thrivya.model.EvidenceReview;
import com.example.thrivya.model.EvidenceReviewOutcome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EvidenceReviewRepository
        extends JpaRepository<EvidenceReview, Long> {

    List<EvidenceReview> findAllByArtifact_IdOrderByReviewedAtDescIdDesc(
            Long artifactId
    );

    Optional<EvidenceReview> findFirstByArtifact_IdOrderByReviewedAtDescIdDesc(
            Long artifactId
    );

    List<EvidenceReview> findAllByReviewer_IdOrderByReviewedAtDescIdDesc(
            Long reviewerId
    );

    /*
     * Reviews concerning evidence owned by a student.
     */
    List<EvidenceReview> findAllByArtifact_Claim_User_IdOrderByReviewedAtDescIdDesc(
            Long studentId
    );

    List<EvidenceReview> findAllByArtifact_IdInOrderByReviewedAtDescIdDesc(
            Collection<Long> artifactIds
    );

    Optional<EvidenceReview> findByIdAndReviewer_Id(
            Long reviewId,
            Long reviewerId
    );

    List<EvidenceReview> findAllByArtifact_IdAndOutcomeOrderByReviewedAtDescIdDesc(
            Long artifactId,
            EvidenceReviewOutcome outcome
    );

    List<EvidenceReview> findAllBySupersedesReview_IdOrderByReviewedAtDescIdDesc(
            Long reviewId
    );

    boolean existsBySupersedesReview_Id(
            Long reviewId
    );
}