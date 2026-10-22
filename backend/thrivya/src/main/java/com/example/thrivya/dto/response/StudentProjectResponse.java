package com.example.thrivya.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProjectResponse {

    private Long studentProjectId;
    private String title;
    private String roleTitle;
    private String status;
    private String studentDescription;
    private String githubUrl;
    private String liveDemoUrl;
    private String evidenceText;
    private List<String> skills;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}