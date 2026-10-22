package com.example.thrivya.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "student_project_skills",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_project_skill",
                        columnNames = {
                                "student_project_id",
                                "skill_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProjectSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_project_id",
            nullable = false
    )
    private StudentProject studentProject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "evidence_status", nullable = false)
    private String evidenceStatus;
}