package com.example.thrivya.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileShareResponse {

    private Long shareId;

    private Long studentId;

    private Long recruiterId;

    private String recruiterDisplayName;

    private boolean shareSkillClaims;

    private boolean shareEvidenceArtifacts;

    private boolean shareReviewResults;

    private Instant createdAt;

    private Instant expiresAt;

    private Instant revokedAt;

    private boolean active;

    /*
     * Suggested values:
     * ACTIVE
     * EXPIRED
     * REVOKED
     * NOT_YET_ACTIVE
     */
    private String status;

    private String message;
}