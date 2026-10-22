package com.example.thrivya.service;

import com.example.thrivya.dto.request.*;
import com.example.thrivya.dto.response.StudentProjectResponse;
import com.example.thrivya.model.*;
import com.example.thrivya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProjectService {

    private final UserRepository userRepository;
    private final CareerProfileRepository profileRepository;
    private final ProjectSuggestionRepository projectSuggestionRepository;
    private final StudentProjectRepository studentProjectRepository;
    private final StudentProjectSkillRepository studentProjectSkillRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public StudentProjectResponse selectProject(
            String email,
            SelectProjectRequest request
    ) {
        User user = getUser(email);

        CareerProfile profile =
                profileRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Career profile not found"
                                ));

        ProjectSuggestion suggestion =
                projectSuggestionRepository
                        .findById(request.getSuggestionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Project suggestion not found"
                                ));

        StudentProject studentProject =
                StudentProject.builder()
                        .user(user)
                        .projectSuggestion(suggestion)
                        .role(profile.getSelectedRole())
                        .title(suggestion.getTitle())
                        .studentDescription(
                                "Project selected. Add your implementation details as you progress."
                        )
                        .status(ProjectStatus.SELECTED)
                        .build();

        StudentProject savedProject =
                studentProjectRepository.save(
                        studentProject
                );

        List<Skill> skills =
                resolveSkills(
                        suggestion.getSkillsCovered()
                );

        for (Skill skill : skills) {
            studentProjectSkillRepository.save(
                    StudentProjectSkill.builder()
                            .studentProject(savedProject)
                            .skill(skill)
                            .evidenceStatus("PLANNED")
                            .build()
            );
        }

        return toResponse(savedProject, skills);
    }

    @Transactional
    public StudentProjectResponse updateEvidence(
            String email,
            Long projectId,
            UpdateProjectEvidenceRequest request
    ) {
        User user = getUser(email);

        StudentProject project =
                studentProjectRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student project not found"
                                ));

        if (!project.getUser().getId()
                .equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You cannot update another student's project"
            );
        }

        project.setStudentDescription(
                request.getStudentDescription()
        );

        project.setGithubUrl(
                request.getGithubUrl()
        );

        project.setLiveDemoUrl(
                request.getLiveDemoUrl()
        );

        project.setEvidenceText(
                request.getEvidenceText()
        );

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getStatus()
                == ProjectStatus.COMPLETED
                || request.getStatus()
                == ProjectStatus.VERIFIED) {
            project.setCompletedAt(
                    LocalDateTime.now()
            );
        }

        StudentProject savedProject =
                studentProjectRepository.save(project);

        List<StudentProjectSkill> projectSkills =
                studentProjectSkillRepository
                        .findAllByStudentProjectId(
                                savedProject.getId()
                        );

        if (request.getEvidenceText() != null
                && !request.getEvidenceText().isBlank()) {
            projectSkills.forEach(projectSkill ->
                    projectSkill.setEvidenceStatus(
                            "USER_CONFIRMED"
                    )
            );

            studentProjectSkillRepository.saveAll(
                    projectSkills
            );
        }

        return toResponse(
                savedProject,
                projectSkills.stream()
                        .map(
                                StudentProjectSkill::getSkill
                        )
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<StudentProjectResponse> getMyProjects(
            String email
    ) {
        User user = getUser(email);

        return studentProjectRepository
                .findAllByUserIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(project ->
                        toResponse(
                                project,
                                studentProjectSkillRepository
                                        .findAllByStudentProjectId(
                                                project.getId()
                                        )
                                        .stream()
                                        .map(
                                                StudentProjectSkill::getSkill
                                        )
                                        .toList()
                        )
                )
                .toList();
    }

    private List<Skill> resolveSkills(
            String skillsCovered
    ) {
        if (skillsCovered == null
                || skillsCovered.isBlank()) {
            return List.of();
        }

        return Arrays.stream(
                        skillsCovered.split("[,\\n]")
                )
                .map(String::trim)
                .filter(skillName ->
                        !skillName.isBlank()
                )
                .map(skillName ->
                        skillRepository
                                .findByCanonicalNameIgnoreCase(
                                        skillName
                                )
                                .orElseGet(() ->
                                        skillRepository.save(
                                                Skill.builder()
                                                        .canonicalName(
                                                                skillName
                                                        )
                                                        .build()
                                        )
                                )
                )
                .toList();
    }

    private StudentProjectResponse toResponse(
            StudentProject project,
            List<Skill> skills
    ) {
        return StudentProjectResponse.builder()
                .studentProjectId(project.getId())
                .title(project.getTitle())
                .roleTitle(
                        project.getRole() == null
                                ? null
                                : project.getRole().getTitle()
                )
                .status(project.getStatus().name())
                .studentDescription(
                        project.getStudentDescription()
                )
                .githubUrl(project.getGithubUrl())
                .liveDemoUrl(project.getLiveDemoUrl())
                .evidenceText(project.getEvidenceText())
                .skills(
                        skills.stream()
                                .map(
                                        Skill::getCanonicalName
                                )
                                .toList()
                )
                .startedAt(project.getStartedAt())
                .completedAt(project.getCompletedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        ));
    }
}