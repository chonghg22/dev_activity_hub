package com.devactivityhub.publicview.project.dto;

import com.devactivityhub.project.domain.Project;

import java.time.LocalDate;

public record PublicProjectDetailResponse(
        String name,
        String slug,
        String description,
        String category,
        LocalDate startedOn,
        LocalDate endedOn
) {
    public static PublicProjectDetailResponse from(Project project) {
        return new PublicProjectDetailResponse(
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getCategory(),
                project.getStartedOn(),
                project.getEndedOn()
        );
    }
}
