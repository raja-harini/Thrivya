package com.example.thrivya.service;

import com.example.thrivya.dto.response.GeneratedEvidenceResponse;
import com.example.thrivya.model.ProfileSkill;
import com.example.thrivya.model.StudentProject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EvidenceGuardrailService {

    public List<GeneratedEvidenceResponse> verifySkills(
            List<ProfileSkill> profileSkills
    ) {
        List<GeneratedEvidenceResponse> results =
                new ArrayList<>();

        for (ProfileSkill profileSkill : profileSkills) {
            String status = determineStatus(profileSkill);

            results.add(
                    GeneratedEvidenceResponse.builder()
                            .claim(
                                    profileSkill.getSkill()
                                            .getCanonicalName()
                            )
                            .status(status)
                            .source(
                                    profileSkill.getSource()
                            )
                            .evidenceText(
                                    profileSkill.getEvidenceText()
                            )
                            .requiresUserConfirmation(
                                    !"VERIFIED".equals(status)
                            )
                            .build()
            );
        }

        return results;
    }

    public GeneratedEvidenceResponse verifyProject(
            StudentProject project
    ) {
        boolean hasDescription =
                project.getStudentDescription() != null
                        && !project.getStudentDescription()
                        .isBlank();

        boolean hasEvidence =
                project.getEvidenceText() != null
                        && !project.getEvidenceText()
                        .isBlank();

        boolean hasRepository =
                project.getGithubUrl() != null
                        && !project.getGithubUrl()
                        .isBlank();

        String status =
                hasDescription && (hasEvidence || hasRepository)
                        ? "USER_CONFIRMED"
                        : "REQUIRES_CONFIRMATION";

        return GeneratedEvidenceResponse.builder()
                .claim(project.getTitle())
                .status(status)
                .source("PROJECT")
                .evidenceText(
                        project.getEvidenceText()
                )
                .requiresUserConfirmation(true)
                .build();
    }

    public boolean isSafeForAutomaticResumeUse(
            GeneratedEvidenceResponse evidence
    ) {
        return "VERIFIED".equals(
                evidence.getStatus()
        );
    }

    public boolean isSafeForDraftWithReview(
            GeneratedEvidenceResponse evidence
    ) {
        return "VERIFIED".equals(
                evidence.getStatus()
        ) || "USER_CONFIRMED".equals(
                evidence.getStatus()
        );
    }

    private String determineStatus(
            ProfileSkill profileSkill
    ) {
        String source =
                profileSkill.getSource();

        String evidenceText =
                profileSkill.getEvidenceText();

        Double confidence =
                profileSkill.getConfidence();

        if (evidenceText == null
                || evidenceText.isBlank()) {
            return "REQUIRES_CONFIRMATION";
        }

        if ("RESUME_TEXT".equals(source)
                || "PROJECT".equals(source)) {
            return "VERIFIED";
        }

        if ("MANUAL_PROGRESS".equals(source)
                && confidence != null
                && confidence >= 0.60) {
            return "USER_CONFIRMED";
        }

        if ("MANUAL_INPUT".equals(source)
                && confidence != null
                && confidence >= 0.80) {
            return "USER_CONFIRMED";
        }

        return "INFERRED";
    }
}