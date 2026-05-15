package com.devactivityhub.publicview.project.dto;

import com.devactivityhub.project.domain.Project;

public record PublicProjectListItemResponse(
        String name,
        String slug,
        String description,
        String category
) {
    public static PublicProjectListItemResponse from(Project project) {
        return new PublicProjectListItemResponse(
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getCategory()
        );
    }
}
