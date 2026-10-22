package com.example.thrivya.service;

import com.example.thrivya.dto.request.CreateProfileShareRequest;
import com.example.thrivya.dto.response.ProfileShareResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.ProfileShareGrantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.example.thrivya.service.EvidenceAccessService.error;

@Service
@RequiredArgsConstructor
public class ProfileShareService {

    private final ProfileShareGrantRepository shareRepository;
    private final EvidenceAccessService access;
    private final EntityManager entityManager;

    @Value("${thrivya.evidence.max-share-days:30}")
    private long maxShareDays;

    @Transactional
    public ProfileShareResponse create(
            String email,
            CreateProfileShareRequest request
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        /*
         * Serialize share replacement for this student.
         */
        entityManager.lock(
                student,
                LockModeType.PESSIMISTIC_WRITE
        );

        User recruiter =
                access.requireApprovedRecruiter(
                        request.getRecruiterId()
                );

        if (student.getId().equals(recruiter.getId())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "A student cannot share with themselves"
            );
        }

        Instant now = Instant.now();

        if (maxShareDays <= 0) {
            throw error(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Invalid sharing-duration configuration"
            );
        }

        Instant latestAllowed =
                now.plus(
                        Duration.ofDays(maxShareDays)
                );

        if (request.getExpiresAt() == null
                || !request.getExpiresAt().isAfter(now)
                || request.getExpiresAt()
                .isAfter(latestAllowed)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Expiry must be in the future and within "
                            + maxShareDays
                            + " days"
            );
        }

        /*
         * Replace existing active grants for this pair.
         * Permissions are never unioned across old grants.
         */
        List<ProfileShareGrant> existing =
                shareRepository.findActiveGrants(
                        student.getId(),
                        recruiter.getId(),
                        now
                );

        for (ProfileShareGrant grant : existing) {
            grant.revoke();

            access.audit(
                    student,
                    "PROFILE_SHARE_GRANT",
                    grant.getId(),
                    "SHARE_REVOKED",
                    "Previous grant replaced by a new sharing decision."
            );
        }

        shareRepository.saveAll(existing);
        shareRepository.flush();

        ProfileShareGrant saved =
                shareRepository.saveAndFlush(
                        ProfileShareGrant.builder()
                                .student(student)
                                .recruiter(recruiter)
                                .shareSkillClaims(
                                        request.isShareSkillClaims()
                                )
                                .shareEvidenceArtifacts(
                                        request.isShareEvidenceArtifacts()
                                )
                                .shareReviewResults(
                                        request.isShareReviewResults()
                                )
                                .expiresAt(
                                        request.getExpiresAt()
                                )
                                .build()
                );

        access.audit(
                student,
                "PROFILE_SHARE_GRANT",
                saved.getId(),
                "PROFILE_SHARED",
                "Student granted time-limited access to the current permitted career profile."
        );

        return access.toShareResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProfileShareResponse> getMyShares(
            String email
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        return shareRepository
                .findAllByStudent_IdOrderByCreatedAtDescIdDesc(
                        student.getId()
                )
                .stream()
                .map(access::toShareResponse)
                .toList();
    }

    @Transactional
    public ProfileShareResponse revoke(
            String email,
            Long shareId
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        entityManager.lock(
                student,
                LockModeType.PESSIMISTIC_WRITE
        );

        ProfileShareGrant grant =
                shareRepository
                        .findByIdAndStudent_Id(
                                shareId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Sharing grant not found"
                                ));

        if (grant.getRevokedAt() == null) {
            grant.revoke();
            shareRepository.saveAndFlush(grant);

            access.audit(
                    student,
                    "PROFILE_SHARE_GRANT",
                    grant.getId(),
                    "SHARE_REVOKED",
                    "Student revoked recruiter access."
            );
        }

        return access.toShareResponse(grant);
    }

    @Transactional(readOnly = true)
    public ProfileShareGrant requireActiveGrant(
            User recruiter,
            Long studentId
    ) {
        access.requireApprovedRecruiter(
                recruiter.getId()
        );

        access.requireStudent(studentId);

        List<ProfileShareGrant> active =
                shareRepository.findActiveGrants(
                        studentId,
                        recruiter.getId(),
                        Instant.now()
                );

        if (active.isEmpty()) {
            throw error(
                    HttpStatus.FORBIDDEN,
                    "No active sharing permission for this student"
            );
        }

        /*
         * Defensive policy for any pre-existing duplicates:
         * use the newest applicable grant, never combine them.
         */
        return active.get(0);
    }
}