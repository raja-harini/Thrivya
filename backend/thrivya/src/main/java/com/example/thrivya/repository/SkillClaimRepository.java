package com.example.thrivya.repository;

import com.example.thrivya.model.SkillClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillClaimRepository
        extends JpaRepository<SkillClaim, Long> {

    List<SkillClaim> findAllByUser_IdOrderByCreatedAtDescIdDesc(
            Long userId
    );

    List<SkillClaim> findAllByUser_IdAndSkill_IdOrderByCreatedAtDescIdDesc(
            Long userId,
            Long skillId
    );

    Optional<SkillClaim> findByIdAndUser_Id(
            Long claimId,
            Long userId
    );

    boolean existsByIdAndUser_Id(
            Long claimId,
            Long userId
    );

    /*
     * Returns claim revisions that directly replace
     * the specified claim.
     */
    List<SkillClaim> findAllBySupersedesClaim_IdOrderByCreatedAtDescIdDesc(
            Long claimId
    );

    boolean existsBySupersedesClaim_Id(
            Long claimId
    );

    List<SkillClaim> findAllByUser_IdAndSourceTypeAndSourceRecordIdOrderByCreatedAtDescIdDesc(
            Long userId,
            String sourceType,
            Long sourceRecordId
    );
}