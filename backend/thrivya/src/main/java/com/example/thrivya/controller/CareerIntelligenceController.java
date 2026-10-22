package com.example.thrivya.controller;

import com.example.thrivya.dto.request.*;
import com.example.thrivya.dto.response.*;
import com.example.thrivya.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/career-profile")
@RequiredArgsConstructor
public class CareerIntelligenceController {

    private final SkillClaimService skillClaimService;
    private final EvidenceSubmissionService evidenceSubmissionService;
    private final CareerIntelligenceService careerIntelligenceService;
    private final ProfileShareService profileShareService;

    @GetMapping
    public CareerIntelligenceProfileResponse getMyProfile(
            Authentication authentication
    ) {
        return careerIntelligenceService.getMyProfile(
                authentication.getName()
        );
    }

    @PostMapping("/claims")
    public ResponseEntity<CareerSkillResponse> createClaim(
            Authentication authentication,
            @Valid @RequestBody
            CreateSkillClaimRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        skillClaimService.create(
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping("/claims")
    public List<CareerSkillResponse> getMyClaims(
            Authentication authentication
    ) {
        return skillClaimService.getMyClaims(
                authentication.getName()
        );
    }

    @PostMapping("/claims/{claimId}/evidence")
    public ResponseEntity<EvidenceDetailResponse> submitEvidence(
            Authentication authentication,
            @PathVariable Long claimId,
            @Valid @RequestBody
            SubmitEvidenceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        evidenceSubmissionService.submit(
                                authentication.getName(),
                                claimId,
                                request
                        )
                );
    }

    @GetMapping("/claims/{claimId}/evidence")
    public List<EvidenceDetailResponse> listClaimEvidence(
            Authentication authentication,
            @PathVariable Long claimId
    ) {
        return evidenceSubmissionService.listForClaim(
                authentication.getName(),
                claimId
        );
    }

    @GetMapping("/evidence/{artifactId}")
    public EvidenceDetailResponse getMyArtifact(
            Authentication authentication,
            @PathVariable Long artifactId
    ) {
        return evidenceSubmissionService.getMyArtifact(
                authentication.getName(),
                artifactId
        );
    }

    @PostMapping("/shares")
    public ResponseEntity<ProfileShareResponse> createShare(
            Authentication authentication,
            @Valid @RequestBody
            CreateProfileShareRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        profileShareService.create(
                                authentication.getName(),
                                request
                        )
                );
    }

    @GetMapping("/shares")
    public List<ProfileShareResponse> getMyShares(
            Authentication authentication
    ) {
        return profileShareService.getMyShares(
                authentication.getName()
        );
    }

    @DeleteMapping("/shares/{shareId}")
    public ProfileShareResponse revokeShare(
            Authentication authentication,
            @PathVariable Long shareId
    ) {
        return profileShareService.revoke(
                authentication.getName(),
                shareId
        );
    }
}