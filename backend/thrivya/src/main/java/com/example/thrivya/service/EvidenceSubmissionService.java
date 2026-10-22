package com.example.thrivya.service;

import com.example.thrivya.dto.request.SubmitEvidenceRequest;
import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import static com.example.thrivya.service.EvidenceAccessService.error;

@Service
@RequiredArgsConstructor
public class EvidenceSubmissionService {

    private final SkillClaimRepository claimRepository;
    private final EvidenceArtifactRepository artifactRepository;
    private final EvidenceAccessService access;

    @Transactional
    public EvidenceDetailResponse submit(
            String email,
            Long claimId,
            SubmitEvidenceRequest request
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        SkillClaim claim = claimRepository
                .findByIdAndUser_Id(
                        claimId,
                        student.getId()
                )
                .orElseThrow(() ->
                        error(
                                HttpStatus.NOT_FOUND,
                                "Claim not found"
                        ));

        if (claimRepository
                .existsBySupersedesClaim_Id(claimId)) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Submit evidence against the current claim revision"
            );
        }

        URI externalUri = requireHttpsUrl(
                request.getExternalUrl()
        );

        if ("REPOSITORY_REFERENCE".equals(
                request.getArtifactType()
        )) {
            String host = externalUri.getHost()
                    .toLowerCase(Locale.ROOT);

            if (!host.equals("github.com")
                    && !host.equals("gitlab.com")
                    && !host.equals("bitbucket.org")) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Repository references must use GitHub, GitLab, or Bitbucket for this MVP"
                );
            }
        }

        EvidenceArtifact previous = null;

        if (request.getSupersedesArtifactId() != null) {
            previous = artifactRepository
                    .findByIdAndClaim_User_Id(
                            request.getSupersedesArtifactId(),
                            student.getId()
                    )
                    .orElseThrow(() ->
                            error(
                                    HttpStatus.NOT_FOUND,
                                    "Previous artifact not found"
                            ));

            if (!previous.getClaim().getId()
                    .equals(claimId)) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Artifact revisions must belong to the same claim"
                );
            }

            if (artifactRepository
                    .existsBySupersedesArtifact_Id(
                            previous.getId()
                    )) {
                throw error(
                        HttpStatus.CONFLICT,
                        "This artifact already has a revision"
                );
            }
        }

        EvidenceArtifact saved =
                artifactRepository.saveAndFlush(
                        EvidenceArtifact.builder()
                                .claim(claim)
                                .artifactType(
                                        request.getArtifactType()
                                )
                                .externalUrl(
                                        externalUri.toASCIIString()
                                )
                                .artifactDescription(
                                        request.getArtifactDescription().trim()
                                )
                                .claimedContribution(
                                        request.getClaimedContribution().trim()
                                )
                                .artifactLocator(
                                        blankToNull(
                                                request.getArtifactLocator()
                                        )
                                )
                                .commitSha(
                                        blankToNull(
                                                request.getCommitSha()
                                        )
                                )
                                .storageKey(null)
                                .contentSha256(null)
                                .contentCapturedAt(null)
                                .supersedesArtifact(previous)
                                .build()
                );

        access.audit(
                student,
                "EVIDENCE_ARTIFACT",
                saved.getId(),
                "EVIDENCE_SUBMITTED",
                "Student submitted an external evidence reference; no automatic authenticity check was performed."
        );

        return access.toEvidenceDetail(saved, true);
    }

    @Transactional(readOnly = true)
    public List<EvidenceDetailResponse> listForClaim(
            String email,
            Long claimId
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        if (!claimRepository.existsByIdAndUser_Id(
                claimId,
                student.getId()
        )) {
            throw error(
                    HttpStatus.NOT_FOUND,
                    "Claim not found"
            );
        }

        return artifactRepository
                .findAllByClaim_IdOrderBySubmittedAtDescIdDesc(
                        claimId
                )
                .stream()
                .map(artifact ->
                        access.toEvidenceDetail(
                                artifact,
                                true
                        ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EvidenceDetailResponse getMyArtifact(
            String email,
            Long artifactId
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        EvidenceArtifact artifact =
                artifactRepository
                        .findByIdAndClaim_User_Id(
                                artifactId,
                                student.getId()
                        )
                        .orElseThrow(() ->
                                error(
                                        HttpStatus.NOT_FOUND,
                                        "Artifact not found"
                                ));

        return access.toEvidenceDetail(artifact, true);
    }

    private URI requireHttpsUrl(String value) {
        try {
            URI uri = URI.create(
                    value == null ? "" : value.trim()
            );

            if (!"https".equalsIgnoreCase(
                    uri.getScheme()
            ) || uri.getHost() == null
                    || uri.getUserInfo() != null) {
                throw new IllegalArgumentException();
            }

            return uri;
        } catch (IllegalArgumentException exception) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "A valid HTTPS evidence URL is required"
            );
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}