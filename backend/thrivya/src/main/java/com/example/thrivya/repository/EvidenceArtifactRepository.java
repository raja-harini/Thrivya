package com.example.thrivya.repository;

import com.example.thrivya.model.EvidenceArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EvidenceArtifactRepository
        extends JpaRepository<EvidenceArtifact, Long> {

    List<EvidenceArtifact> findAllByClaim_IdOrderBySubmittedAtDescIdDesc(
            Long claimId
    );

    /*
     * Student ownership check:
     * artifact -> claim -> user.
     */
    Optional<EvidenceArtifact> findByIdAndClaim_User_Id(
            Long artifactId,
            Long userId
    );

    boolean existsByIdAndClaim_User_Id(
            Long artifactId,
            Long userId
    );

    List<EvidenceArtifact> findAllByClaim_User_IdOrderBySubmittedAtDescIdDesc(
            Long userId
    );

    /*
     * Useful when building a profile from several claims.
     * Avoid calling this with an empty collection.
     */
    List<EvidenceArtifact> findAllByClaim_IdInOrderBySubmittedAtDescIdDesc(
            Collection<Long> claimIds
    );

    List<EvidenceArtifact> findAllBySupersedesArtifact_IdOrderBySubmittedAtDescIdDesc(
            Long artifactId
    );

    boolean existsBySupersedesArtifact_Id(
            Long artifactId
    );

    /*
     * Matching hashes can identify matching captured content.
     * They do not establish authorship or competence.
     */
    List<EvidenceArtifact> findAllByContentSha256(
            String contentSha256
    );
}