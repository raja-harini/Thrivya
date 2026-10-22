package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "project_goal", columnDefinition = "TEXT")
    private String projectGoal;

    @Column(name = "skills_covered", columnDefinition = "TEXT")
    private String skillsCovered;

    @Column(name = "implementation_steps", columnDefinition = "TEXT")
    private String implementationSteps;

    @Column(name = "evidence_required", columnDefinition = "TEXT")
    private String evidenceRequired;

    @Column(name = "resume_template", columnDefinition = "TEXT")
    private String resumeTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectDifficulty difficulty;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "technology_trend_notes", columnDefinition = "TEXT")
    private String technologyTrendNotes;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    public void beforeCreate() {
        if (active == null) {
            active = true;
        }
    }
}