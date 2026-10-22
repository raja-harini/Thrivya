package com.example.thrivya.controller;

import com.example.thrivya.dto.response.CareerIntelligenceProfileResponse;
import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.service.CareerIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter/candidates")
@RequiredArgsConstructor
public class RecruiterCareerProfileController {

    private final CareerIntelligenceService careerIntelligenceService;

    @GetMapping("/{studentId}/career-profile")
    public CareerIntelligenceProfileResponse getProfile(
            Authentication authentication,
            @PathVariable Long studentId
    ) {
        return careerIntelligenceService
                .getRecruiterProfile(
                        authentication.getName(),
                        studentId
                );
    }

    @GetMapping("/{studentId}/evidence/{artifactId}")
    public EvidenceDetailResponse getEvidence(
            Authentication authentication,
            @PathVariable Long studentId,
            @PathVariable Long artifactId
    ) {
        return careerIntelligenceService
                .getRecruiterEvidence(
                        authentication.getName(),
                        studentId,
                        artifactId
                );
    }
}