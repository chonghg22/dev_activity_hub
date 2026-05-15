package com.devactivityhub.project.dto;

import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80) String slug,
        @Size(max = 5000) String description,
        @NotBlank @Size(max = 50) String category,
        @NotNull ProjectVisibility visibility,
        Boolean isPublic,
        ProjectStatus status,
        LocalDate startedOn,
        LocalDate endedOn
) {
}
