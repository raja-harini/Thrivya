package com.example.thrivya.config;

import com.example.thrivya.model.*;
import com.example.thrivya.repository.ProjectSuggestionRepository;
import com.example.thrivya.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ProjectSuggestionDataInitializer {

    private final RoleRepository roleRepository;
    private final ProjectSuggestionRepository projectSuggestionRepository;

    @Bean
    public CommandLineRunner seedProjectSuggestions() {
        return args -> {
            addJavaBackendProjects();
            addFullStackProjects();
            addDataAnalystProjects();
        };
    }

    private void addJavaBackendProjects() {
        Role role = roleRepository
                .findByTitleIgnoreCase(
                        "Java Backend Developer"
                )
                .orElse(null);

        if (role == null) {
            return;
        }

        saveIfMissing(
                role,
                "Containerized Spring Boot REST API",
                "Build a task-management REST API and run it in Docker.",
                "Strengthen backend development, API design, database usage, and Docker evidence.",
                "Java, Spring Boot, REST API, PostgreSQL, Docker, Git",
                """
                1. Create Spring Boot REST endpoints.
                2. Add PostgreSQL persistence.
                3. Add validation and exception handling.
                4. Write a Dockerfile.
                5. Run the application with Docker.
                6. Publish the source code and README on GitHub.
                """,
                """
                GitHub repository URL
                README with setup steps
                Dockerfile
                API endpoint screenshots or Postman collection
                Student-written description of what was built
                """,
                """
                • Built a containerized Spring Boot REST API using Java, PostgreSQL, and Docker.
                • Implemented CRUD endpoints and documented local setup steps in the project README.
                """,
                ProjectDifficulty.INTERMEDIATE,
                18,
                "Containerization and deployable backend projects remain valuable evidence for backend roles."
        );

        saveIfMissing(
                role,
                "JWT-secured Student Profile API",
                "Build a backend API where students can register, log in, and access protected profile endpoints.",
                "Strengthen Spring Security, JWT, REST APIs, and evidence for secure backend work.",
                "Java, Spring Boot, Spring Security, JWT, REST API, PostgreSQL",
                """
                1. Create user registration and login endpoints.
                2. Hash passwords using BCrypt.
                3. Generate JWT tokens.
                4. Protect profile endpoints by role.
                5. Add PostgreSQL persistence.
                6. Write API documentation and README.
                """,
                """
                GitHub repository URL
                Security configuration screenshots
                Postman collection
                README with authentication flow
                Student-written description of implemented security features
                """,
                """
                • Implemented JWT-based authentication and role-protected REST endpoints using Spring Boot and PostgreSQL.
                • Added password hashing and documented API authentication testing with Postman.
                """,
                ProjectDifficulty.INTERMEDIATE,
                20,
                "Authentication, authorization, and API security are foundational backend capabilities."
        );
    }

    private void addFullStackProjects() {
        Role role = roleRepository
                .findByTitleIgnoreCase(
                        "Full Stack Developer"
                )
                .orElse(null);

        if (role == null) {
            return;
        }

        saveIfMissing(
                role,
                "Full Stack Career Profile Dashboard",
                "Build a React dashboard connected to a Spring Boot API for managing student skills and projects.",
                "Strengthen frontend-backend integration and demonstrate complete application delivery.",
                "React, JavaScript, Spring Boot, REST API, PostgreSQL, Git",
                """
                1. Build a React dashboard.
                2. Create Spring Boot REST APIs.
                3. Connect React through Axios.
                4. Store data in PostgreSQL.
                5. Add login and role-based navigation.
                6. Publish source code and screenshots.
                """,
                """
                GitHub repository URL
                React screenshots
                API screenshots
                README with local setup
                Student-written feature explanation
                """,
                """
                • Developed a full stack dashboard using React, Spring Boot, and PostgreSQL.
                • Integrated role-based access and REST APIs for profile management.
                """,
                ProjectDifficulty.INTERMEDIATE,
                24,
                "Full-stack project evidence demonstrates the ability to deliver an end-to-end feature."
        );
    }

    private void addDataAnalystProjects() {
        Role role = roleRepository
                .findByTitleIgnoreCase(
                        "Data Analyst"
                )
                .orElse(null);

        if (role == null) {
            return;
        }

        saveIfMissing(
                role,
                "Student Placement Data Analysis Dashboard",
                "Analyze placement-related data and build visual reports showing skills, readiness, and trends.",
                "Strengthen SQL, Python, data cleaning, visualization, and analytical storytelling.",
                "Python, SQL, Statistics, Data Visualization, Git",
                """
                1. Select an open placement or job-skills dataset.
                2. Clean missing values.
                3. Write SQL queries for analysis.
                4. Create charts using Python.
                5. Document findings and assumptions.
                6. Publish the notebook and README.
                """,
                """
                GitHub repository URL
                Dataset source link
                Jupyter notebook
                Screenshots of visualizations
                Student-written analysis summary
                """,
                """
                • Analyzed placement-related data using SQL and Python to identify skill and readiness patterns.
                • Created visual reports and documented data-cleaning assumptions.
                """,
                ProjectDifficulty.INTERMEDIATE,
                16,
                "Data cleaning, SQL analysis, and visual storytelling are central evidence for entry-level data roles."
        );
    }

    private void saveIfMissing(
            Role role,
            String title,
            String shortDescription,
            String goal,
            String skills,
            String steps,
            String evidence,
            String resumeTemplate,
            ProjectDifficulty difficulty,
            Integer hours,
            String technologyTrendNotes
    ) {
        boolean exists =
                projectSuggestionRepository
                        .findAllByRoleIdAndActiveTrue(role.getId())
                        .stream()
                        .anyMatch(project ->
                                project.getTitle()
                                        .equalsIgnoreCase(title)
                        );

        if (!exists) {
            projectSuggestionRepository.save(
                    ProjectSuggestion.builder()
                            .role(role)
                            .title(title)
                            .shortDescription(shortDescription)
                            .projectGoal(goal)
                            .skillsCovered(skills)
                            .implementationSteps(steps)
                            .evidenceRequired(evidence)
                            .resumeTemplate(resumeTemplate)
                            .difficulty(difficulty)
                            .estimatedHours(hours)
                            .technologyTrendNotes(technologyTrendNotes)
                            .active(true)
                            .build()
            );
        }
    }
}