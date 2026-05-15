package com.devactivityhub.activity.external.dto;

import com.devactivityhub.activity.external.domain.ExternalActivity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ExternalActivityResponse(
        Long id,
        Long projectId,
        String projectSlug,
        Long projectSourceId,
        String sourceType,
        String sourceId,
        String activityType,
        String title,
        String contentSummary,
        String actorName,
        String activityUrl,
        LocalDate activityDate,
        OffsetDateTime occurredAt,
        boolean isPublic,
        OffsetDateTime createdAt
) {
    public static ExternalActivityResponse from(ExternalActivity externalActivity) {
        return new ExternalActivityResponse(
                externalActivity.getId(),
                externalActivity.getProject() == null ? null : externalActivity.getProject().getId(),
                externalActivity.getProject() == null ? null : externalActivity.getProject().getSlug(),
                externalActivity.getProjectSource() == null ? null : externalActivity.getProjectSource().getId(),
                externalActivity.getSourceType(),
                externalActivity.getSourceId(),
                externalActivity.getActivityType(),
                externalActivity.getTitle(),
                externalActivity.getContentSummary(),
                externalActivity.getActorName(),
                externalActivity.getActivityUrl(),
                externalActivity.getOccurredAt() == null ? null : externalActivity.getOccurredAt().toLocalDate(),
                externalActivity.getOccurredAt(),
                externalActivity.isPublic(),
                externalActivity.getCreatedAt()
        );
    }
}
