package com.example.thrivya.service;

import com.example.thrivya.dto.response.ProjectSuggestionResponse;
import com.example.thrivya.model.CareerProfile;
import com.example.thrivya.model.ProjectSuggestion;
import com.example.thrivya.model.SkillGap;
import com.example.thrivya.model.User;
import com.example.thrivya.repository.CareerProfileRepository;
import com.example.thrivya.repository.ProjectSuggestionRepository;
import com.example.thrivya.repository.SkillGapRepository;
import com.example.thrivya.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectSuggestionService {

    private final UserRepository userRepository;
    private final CareerProfileRepository profileRepository;
    private final SkillGapRepository skillGapRepository;
    private final ProjectSuggestionRepository projectSuggestionRepository;

    /**
     * Returns up to three projects for one skill in the student's
     * latest skill-gap analysis.
     */
    public List<ProjectSuggestionResponse> getSuggestions(
            String email,
            Long skillGapId
    ) {
        CareerProfile profile = getProfileWithSelectedRole(email);

        if (skillGapId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Skill gap ID is required"
            );
        }

        SkillGap requestedGap = skillGapRepository.findById(skillGapId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Skill gap not found"
                        )
                );

        // Ownership check: a student cannot use another student's gap.
        if (!Objects.equals(
                requestedGap.getProfile().getId(),
                profile.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view project suggestions for another student"
            );
        }

        Long selectedRoleId = profile.getSelectedRole().getId();

        // A historical gap for another role must not be used.
        if (!Objects.equals(
                requestedGap.getRole().getId(),
                selectedRoleId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Your selected role has changed. Refresh your skill-gap analysis."
            );
        }

        List<SkillGap> latestGaps = getLatestGaps(profile);

        /*
         * If React sends an older gap ID, resolve the same skill
         * from the latest analysis rather than using stale percentages.
         */
        SkillGap currentGap = latestGaps.stream()
                .filter(gap ->
                        Objects.equals(
                                gap.getSkill().getId(),
                                requestedGap.getSkill().getId()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "This skill is not in your latest analysis. Refresh the skill-gap page."
                        )
                );

        String selectedSkillName = normalize(
                currentGap.getSkill().getCanonicalName()
        );

        List<ProjectSuggestion> projects =
                projectSuggestionRepository
                        .findAllByRoleIdAndActiveTrue(selectedRoleId);

        return projects.stream()
                .filter(project ->
                        splitLinesOrCommas(project.getSkillsCovered())
                                .stream()
                                .map(this::normalize)
                                .anyMatch(selectedSkillName::equals)
                )
                .map(project -> toResponse(project, latestGaps))
                .sorted(projectRanking())
                .limit(3)
                .toList();
    }

    /**
     * Returns up to five projects for the selected role,
     * ranked using only the latest skill-gap analysis.
     */
    public List<ProjectSuggestionResponse> getAllForRole(
            String email
    ) {
        CareerProfile profile = getProfileWithSelectedRole(email);

        List<SkillGap> latestGaps = getLatestGaps(profile);

        return projectSuggestionRepository
                .findAllByRoleIdAndActiveTrue(
                        profile.getSelectedRole().getId()
                )
                .stream()
                .map(project -> toResponse(project, latestGaps))
                .sorted(projectRanking())
                .limit(5)
                .toList();
    }

    /**
     * Loads the newest analysis version for this profile and role,
     * then loads all gaps within that version.
     */
    private List<SkillGap> getLatestGaps(
            CareerProfile profile
    ) {
        Long profileId = profile.getId();
        Long roleId = profile.getSelectedRole().getId();

        Integer latestVersion =
                skillGapRepository.findLatestAnalysisVersion(
                        profileId,
                        roleId
                );

        if (latestVersion == null || latestVersion <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No skill-gap analysis is available for your selected role. Calculate skill gaps first."
            );
        }

        List<SkillGap> latestGaps =
                skillGapRepository
                        .findAllByProfileIdAndRoleIdAndAnalysisVersionOrderByGapPercentageDescPriorityLevelAsc(
                                profileId,
                                roleId,
                                latestVersion
                        );

        if (latestGaps.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The latest skill-gap analysis is empty. Recalculate your skill gaps."
            );
        }

        return latestGaps;
    }

    private ProjectSuggestionResponse toResponse(
            ProjectSuggestion project,
            List<SkillGap> latestGaps
    ) {
        List<String> skillsCovered =
                splitLinesOrCommas(project.getSkillsCovered());

        Set<String> coveredNames = skillsCovered.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        // Keep backend ordering: largest gaps first.
        List<SkillGap> addressedGaps = latestGaps.stream()
                .filter(gap -> gap.getGapPercentage() != null)
                .filter(gap -> gap.getGapPercentage() > 0)
                .filter(gap ->
                        coveredNames.contains(
                                normalize(
                                        gap.getSkill().getCanonicalName()
                                )
                        )
                )
                .toList();

        List<String> addressedSkills = addressedGaps.stream()
                .map(gap -> gap.getSkill().getCanonicalName())
                .distinct()
                .toList();

        return ProjectSuggestionResponse.builder()
                .suggestionId(project.getId())
                .title(project.getTitle())
                .targetRole(project.getRole().getTitle())
                .shortDescription(project.getShortDescription())
                .projectGoal(project.getProjectGoal())
                .skillsCovered(skillsCovered)
                .skillsAddressed(addressedSkills)
                .reason(buildReason(project, addressedGaps))
                .implementationSteps(
                        splitLines(project.getImplementationSteps())
                )
                .evidenceRequired(
                        splitLines(project.getEvidenceRequired())
                )
                .resumeTemplate(project.getResumeTemplate())
                .difficulty(
                        project.getDifficulty() == null
                                ? null
                                : project.getDifficulty().name()
                )
                .estimatedHours(project.getEstimatedHours())
                .technologyTrendNotes(
                        project.getTechnologyTrendNotes()
                )
                .githubSearchUrl(
                        createGitHubSearchUrl(project.getTitle())
                )
                .build();
    }

    /**
     * Ranking:
     * 1. More current gaps addressed.
     * 2. Lower estimated effort, when available.
     * 3. Title for deterministic tie-breaking.
     */
    private Comparator<ProjectSuggestionResponse> projectRanking() {
        return Comparator
                .comparingInt(
                        (ProjectSuggestionResponse response) ->
                                response.getSkillsAddressed().size()
                )
                .reversed()
                .thenComparing(
                        ProjectSuggestionResponse::getEstimatedHours,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(
                        ProjectSuggestionResponse::getTitle,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );
    }

    private String buildReason(
            ProjectSuggestion project,
            List<SkillGap> addressedGaps
    ) {
        String roleTitle = project.getRole().getTitle();

        if (addressedGaps.isEmpty()) {
            return "This project matches your selected role, "
                    + roleTitle
                    + ", and offers practice in the listed skills. "
                    + "It does not address an outstanding gap in your latest analysis.";
        }

        String gapDetails = addressedGaps.stream()
                .map(gap ->
                        gap.getSkill().getCanonicalName()
                                + " ("
                                + Math.round(gap.getGapPercentage())
                                + "% gap)"
                )
                .collect(Collectors.joining(", "));

        String evidenceDetails = addressedGaps.stream()
                .map(SkillGap::getRationale)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .limit(2)
                .collect(Collectors.joining(" "));

        String reason =
                "This project is recommended for "
                        + roleTitle
                        + " because it covers these gaps in your latest analysis: "
                        + gapDetails
                        + ". Completing the relevant tasks can help you build evidence; "
                        + "selecting the project alone does not demonstrate proficiency.";

        if (!evidenceDetails.isBlank()) {
            reason += " Profile context: " + evidenceDetails;
        }

        return reason;
    }

    private String createGitHubSearchUrl(
            String projectTitle
    ) {
        String query = URLEncoder.encode(
                projectTitle == null ? "" : projectTitle,
                StandardCharsets.UTF_8
        );

        return "https://github.com/search?q="
                + query
                + "&type=repositories";
    }

    private List<String> splitLinesOrCommas(
            String content
    ) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        // Deduplicate skill names while preserving display order.
        Map<String, String> uniqueSkills = Arrays.stream(
                        content.split("[,\\r\\n]+")
                )
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toMap(
                        this::normalize,
                        value -> value,
                        (first, second) -> first,
                        java.util.LinkedHashMap::new
                ));

        return List.copyOf(uniqueSkills.values());
    }

    private List<String> splitLines(
            String content
    ) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        return content.lines()
                .map(String::trim)
                .map(line ->
                        line.replaceFirst(
                                "^\\d+\\.\\s*",
                                ""
                        )
                )
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    private CareerProfile getProfileWithSelectedRole(
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );

        CareerProfile profile =
                profileRepository.findByUserId(user.getId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Career profile not found"
                                )
                        );

        if (profile.getSelectedRole() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Select a target role before viewing project suggestions"
            );
        }

        return profile;
    }
}