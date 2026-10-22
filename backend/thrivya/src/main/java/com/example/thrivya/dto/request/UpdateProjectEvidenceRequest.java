package com.example.thrivya.dto.request;

import com.example.thrivya.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectEvidenceRequest {

    @NotBlank(message = "Project description is required")
    private String studentDescription;

    private String githubUrl;

    private String liveDemoUrl;

    private String evidenceText;

    private ProjectStatus status;
}