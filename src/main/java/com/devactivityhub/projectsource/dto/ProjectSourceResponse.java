package com.devactivityhub.projectsource.dto;

import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;

import java.time.OffsetDateTime;

public record ProjectSourceResponse(
        Long id,
        Long projectId,
        String projectSlug,
        ProjectSourceType sourceType,
        String externalSourceId,
        String externalName,
        String externalUrl,
        boolean isPrimary,
        OffsetDateTime createdAt
) {
    public static ProjectSourceResponse from(ProjectSource projectSource) {
        return new ProjectSourceResponse(
                projectSource.getId(),
                projectSource.getProject().getId(),
                projectSource.getProject().getSlug(),
                projectSource.getSourceType(),
                projectSource.getExternalSourceId(),
                projectSource.getExternalName(),
                projectSource.getExternalUrl(),
                projectSource.isPrimary(),
                projectSource.getCreatedAt()
        );
    }
}
