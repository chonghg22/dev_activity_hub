package com.devactivityhub.project.dto;

import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectUpdateRequest(
        @Size(max = 120) String name,
        @Size(max = 80) String slug,
        @Size(max = 5000) String description,
        @Size(max = 50) String category,
        ProjectVisibility visibility,
        Boolean isPublic,
        ProjectStatus status,
        LocalDate startedOn,
        LocalDate endedOn
) {
}
