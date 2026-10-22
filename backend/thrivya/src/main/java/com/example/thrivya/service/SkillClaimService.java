package com.example.thrivya.service;

import com.example.thrivya.dto.request.CreateSkillClaimRequest;
import com.example.thrivya.dto.response.CareerSkillResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.thrivya.service.EvidenceAccessService.error;

@Service
@RequiredArgsConstructor
public class SkillClaimService {

    private final SkillClaimRepository claimRepository;
    private final SkillRepository skillRepository;
    private final EvidenceAccessService access;
    private final EntityManager entityManager;

    @Transactional
    public CareerSkillResponse create(
            String email,
            CreateSkillClaimRequest request
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        Skill skill = skillRepository
                .findById(request.getSkillId())
                .orElseThrow(() ->
                        error(
                                HttpStatus.NOT_FOUND,
                                "Skill not found"
                        ));

        validateSource(student.getId(), request);

        SkillClaim previous = null;

        if (request.getSupersedesClaimId() != null) {
            previous = claimRepository
                    .findByIdAndUser_Id(
                            request.getSupersedesClaimId(),
                            student.getId()
                    )
                    .orElseThrow(() ->
                            error(
                                    HttpStatus.NOT_FOUND,
                                    "Previous claim not found"
                            ));

            if (!previous.getSkill().getId()
                    .equals(skill.getId())) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "A revised claim must concern the same skill"
                );
            }

            if (claimRepository
                    .existsBySupersedesClaim_Id(
                            previous.getId()
                    )) {
                throw error(
                        HttpStatus.CONFLICT,
                        "This claim already has a revision"
                );
            }
        }

        SkillClaim saved = claimRepository.saveAndFlush(
                SkillClaim.builder()
                        .user(student)
                        .skill(skill)
                        .claimText(
                                request.getClaimText().trim()
                        )
                        .sourceType(request.getSourceType())
                        .sourceRecordId(
                                request.getSourceRecordId()
                        )
                        .supersedesClaim(previous)
                        .build()
        );

        access.audit(
                student,
                "SKILL_CLAIM",
                saved.getId(),
                previous == null
                        ? "CLAIM_CREATED"
                        : "CLAIM_REVISED",
                "Student submitted a self-reported skill claim."
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CareerSkillResponse> getMyClaims(
            String email
    ) {
        User student = access.requireActor(
                email,
                "STUDENT"
        );

        return claimRepository
                .findAllByUser_IdOrderByCreatedAtDescIdDesc(
                        student.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CareerSkillResponse toResponse(
            SkillClaim claim
    ) {
        return CareerSkillResponse.builder()
                .skillId(claim.getSkill().getId())
                .skillName(
                        claim.getSkill().getCanonicalName()
                )
                .claimId(claim.getId())
                .claimText(claim.getClaimText())
                .claimSourceType(
                        claim.getSourceType()
                )
                .claimSourceRecordId(
                        claim.getSourceRecordId()
                )
                .claimCreatedAt(
                        claim.getCreatedAt()
                )
                .category("DEVELOPING")
                .evidenceStatus("SELF_REPORTED")
                .evidenceStatusExplanation(
                        "Claim submission does not establish independent verification. See the career profile for evidence decisions."
                )
                .knowledgeDepthStatus("NOT_ASSESSED")
                .knowledgeDepthExplanation(
                        "No knowledge-depth assessment is implied."
                )
                .build();
    }

    /*
     * These are allowlisted JPQL entity names from
     * the earlier Thrivya implementation.
     *
     * Each source entity must have:
     * id
     * user
     *
     * MANUAL_INPUT does not require an external source.
     */
    private void validateSource(
            Long studentId,
            CreateSkillClaimRequest request
    ) {
        String entityName = switch (
                request.getSourceType()
                ) {
            case "MANUAL_INPUT" -> null;
            case "RESUME_TEXT" -> "Resume";
            case "MANUAL_PROGRESS" -> "SkillProgressUpdate";
            case "STUDENT_PROJECT" -> "StudentProject";
            default -> throw error(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported source type"
            );
        };

        if (entityName == null) {
            if (request.getSourceRecordId() != null) {
                throw error(
                        HttpStatus.BAD_REQUEST,
                        "Manual input must not reference an unrelated source record"
                );
            }

            return;
        }

        if (request.getSourceRecordId() == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Source record ID is required"
            );
        }

        try {
            Long count = entityManager.createQuery(
                            "select count(source) from "
                                    + entityName
                                    + " source where source.id = :id "
                                    + "and source.user.id = :studentId",
                            Long.class
                    )
                    .setParameter(
                            "id",
                            request.getSourceRecordId()
                    )
                    .setParameter(
                            "studentId",
                            studentId
                    )
                    .getSingleResult();

            if (count == 0) {
                throw error(
                        HttpStatus.NOT_FOUND,
                        "Source record not found for this student"
                );
            }
        } catch (IllegalArgumentException exception) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Source entity mapping is unavailable. Check the source entity name and its user relationship."
            );
        }
    }
}