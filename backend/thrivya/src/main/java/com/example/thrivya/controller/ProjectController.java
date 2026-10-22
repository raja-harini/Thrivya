package com.example.thrivya.controller;

import com.example.thrivya.dto.request.*;
import com.example.thrivya.dto.response.*;
import com.example.thrivya.service.ProjectSuggestionService;
import com.example.thrivya.service.StudentProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectSuggestionService projectSuggestionService;
    private final StudentProjectService studentProjectService;

    @GetMapping("/suggestions")
    public ResponseEntity<List<ProjectSuggestionResponse>>
    getSuggestions(
            Authentication authentication,
            @RequestParam Long skillGapId
    ) {
        return ResponseEntity.ok(
                projectSuggestionService.getSuggestions(
                        authentication.getName(),
                        skillGapId
                )
        );
    }

    @GetMapping("/suggestions/role")
    public ResponseEntity<List<ProjectSuggestionResponse>>
    getRoleSuggestions(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectSuggestionService.getAllForRole(
                        authentication.getName()
                )
        );
    }

    @PostMapping("/select")
    public ResponseEntity<StudentProjectResponse>
    selectProject(
            Authentication authentication,
            @Valid @RequestBody SelectProjectRequest request
    ) {
        return ResponseEntity.ok(
                studentProjectService.selectProject(
                        authentication.getName(),
                        request
                )
        );
    }

    @PutMapping("/{projectId}/evidence")
    public ResponseEntity<StudentProjectResponse>
    updateEvidence(
            Authentication authentication,
            @PathVariable Long projectId,
            @Valid @RequestBody
            UpdateProjectEvidenceRequest request
    ) {
        return ResponseEntity.ok(
                studentProjectService.updateEvidence(
                        authentication.getName(),
                        projectId,
                        request
                )
        );
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<StudentProjectResponse>>
    getMyProjects(Authentication authentication) {
        return ResponseEntity.ok(
                studentProjectService.getMyProjects(
                        authentication.getName()
                )
        );
    }
}