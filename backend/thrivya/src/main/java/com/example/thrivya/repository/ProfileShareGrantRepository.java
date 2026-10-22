package com.example.thrivya.repository;

import com.example.thrivya.model.ProfileShareGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProfileShareGrantRepository
        extends JpaRepository<ProfileShareGrant, Long> {

    List<ProfileShareGrant> findAllByStudent_IdOrderByCreatedAtDescIdDesc(
            Long studentId
    );

    List<ProfileShareGrant> findAllByRecruiter_IdOrderByCreatedAtDescIdDesc(
            Long recruiterId
    );

    Optional<ProfileShareGrant> findByIdAndStudent_Id(
            Long shareId,
            Long studentId
    );

    Optional<ProfileShareGrant> findByIdAndRecruiter_Id(
            Long shareId,
            Long recruiterId
    );

    /*
     * Active means:
     * created already
     * not revoked
     * not expired
     *
     * Returns all applicable grants so the service can
     * apply a clear sharing-permission policy.
     */
    @Query("""
            SELECT grantRecord
            FROM ProfileShareGrant grantRecord
            WHERE grantRecord.student.id = :studentId
              AND grantRecord.recruiter.id = :recruiterId
              AND grantRecord.createdAt <= :now
              AND grantRecord.revokedAt IS NULL
              AND grantRecord.expiresAt > :now
            ORDER BY grantRecord.createdAt DESC, grantRecord.id DESC
            """)
    List<ProfileShareGrant> findActiveGrants(
            @Param("studentId") Long studentId,
            @Param("recruiterId") Long recruiterId,
            @Param("now") Instant now
    );

    @Query("""
            SELECT COUNT(grantRecord)
            FROM ProfileShareGrant grantRecord
            WHERE grantRecord.student.id = :studentId
              AND grantRecord.recruiter.id = :recruiterId
              AND grantRecord.createdAt <= :now
              AND grantRecord.revokedAt IS NULL
              AND grantRecord.expiresAt > :now
            """)
    long countActiveGrants(
            @Param("studentId") Long studentId,
            @Param("recruiterId") Long recruiterId,
            @Param("now") Instant now
    );
}