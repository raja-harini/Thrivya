package com.example.thrivya.repository;

import com.example.thrivya.model.SkillGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillGapRepository
        extends JpaRepository<SkillGap, Long> {

    @Query("""
            SELECT COALESCE(MAX(s.analysisVersion), 0)
            FROM SkillGap s
            WHERE s.profile.id = :profileId
              AND s.role.id = :roleId
            """)
    Integer findLatestAnalysisVersion(
            @Param("profileId") Long profileId,
            @Param("roleId") Long roleId
    );

    List<SkillGap>
    findAllByProfileIdAndRoleIdAndAnalysisVersionOrderByGapPercentageDescPriorityLevelAsc(
            Long profileId,
            Long roleId,
            Integer analysisVersion
    );

    @Query("""
            SELECT s
            FROM SkillGap s
            JOIN FETCH s.role
            LEFT JOIN FETCH s.resume
            WHERE s.profile.id = :profileId
              AND s.role.id = :roleId
            ORDER BY s.analysisVersion DESC,
                     s.calculatedAt DESC
            """)
    List<SkillGap> findHistoryWithRoleAndResume(
            @Param("profileId") Long profileId,
            @Param("roleId") Long roleId
    );

    List<SkillGap>
    findAllByProfileIdAndRoleIdOrderByAnalysisVersionDesc(
            Long profileId,
            Long roleId
    );

    Optional<SkillGap>
    findTopByProfileIdAndRoleIdOrderByAnalysisVersionDesc(
            Long profileId,
            Long roleId
    );
}