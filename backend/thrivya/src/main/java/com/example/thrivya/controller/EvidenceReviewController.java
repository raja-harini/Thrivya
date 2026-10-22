package com.example.thrivya.controller;

import com.example.thrivya.dto.request.SubmitEvidenceReviewRequest;
import com.example.thrivya.dto.response.EvidenceDetailResponse;
import com.example.thrivya.service.EvidenceReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviewer/evidence-reviews")
@RequiredArgsConstructor
public class EvidenceReviewController {

    private final EvidenceReviewService evidenceReviewService;

    @GetMapping("/assigned")
    public List<EvidenceDetailResponse> assignedArtifacts(
            Authentication authentication
    ) {
        return evidenceReviewService.assignedArtifacts(
                authentication.getName()
        );
    }

    @GetMapping("/artifacts/{artifactId}")
    public EvidenceDetailResponse getArtifact(
            Authentication authentication,
            @PathVariable Long artifactId
    ) {
        return evidenceReviewService.getAssignedArtifact(
                authentication.getName(),
                artifactId
        );
    }

    @PostMapping("/artifacts/{artifactId}")
    public ResponseEntity<EvidenceDetailResponse> submitReview(
            Authentication authentication,
            @PathVariable Long artifactId,
            @Valid @RequestBody
            SubmitEvidenceReviewRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        evidenceReviewService.submitReview(
                                authentication.getName(),
                                artifactId,
                                request
                        )
                );
    }
}