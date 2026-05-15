package com.devactivityhub.activity.manuallog.dto;

import com.devactivityhub.project.domain.Project;

public record ProjectReferenceResponse(
        Long id,
        String name,
        String slug
) {
    public static ProjectReferenceResponse from(Project project) {
        return new ProjectReferenceResponse(
                project.getId(),
                project.getName(),
                project.getSlug()
        );
    }
}
