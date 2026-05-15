package com.devactivityhub.project.dto;

import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String slug,
        String description,
        String category,
        ProjectVisibility visibility,
        boolean isPublic,
        ProjectStatus status,
        LocalDate startedOn,
        LocalDate endedOn,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getCategory(),
                project.getVisibility(),
                project.isPublic(),
                project.getStatus(),
                project.getStartedOn(),
                project.getEndedOn(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
