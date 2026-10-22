package com.example.thrivya.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectProjectRequest {

    @NotNull(message = "Project suggestion ID is required")
    private Long suggestionId;
}