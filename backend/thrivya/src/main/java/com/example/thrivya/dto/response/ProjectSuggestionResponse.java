package com.example.thrivya.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSuggestionResponse {

    private Long suggestionId;
    private String title;
    private String targetRole;
    private String shortDescription;
    private String projectGoal;
    private List<String> skillsCovered;
    private List<String> skillsAddressed;
    private String reason;
    private List<String> implementationSteps;
    private List<String> evidenceRequired;
    private String resumeTemplate;
    private String difficulty;
    private Integer estimatedHours;
    private String technologyTrendNotes;
    private String githubSearchUrl;
}