package com.devactivityhub.publicview.timeline.dto;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.activity.manuallog.domain.ManualLog;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record PublicTimelineItemResponse(
        Long id,
        String sourceKind,
        OffsetDateTime occurredAt,
        String projectSlug,
        String projectName,
        String title,
        String content,
        String activityType,
        LocalDate workDate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        boolean highlighted,
        Set<String> tags
) {
    public static PublicTimelineItemResponse from(ManualLog manualLog) {
        return new PublicTimelineItemResponse(
                manualLog.getId(),
                "MANUAL_LOG",
                timelineTimeOf(manualLog),
                manualLog.getProject().getSlug(),
                manualLog.getProject().getName(),
                manualLog.getTitle(),
                manualLog.getContent(),
                manualLog.getActivityType().name(),
                manualLog.getWorkDate(),
                manualLog.getStartedAt(),
                manualLog.getEndedAt(),
                manualLog.isHighlighted(),
                manualLog.getTags().stream().map(tag -> tag.getName()).collect(Collectors.toSet())
        );
    }

    public static PublicTimelineItemResponse from(ExternalActivity externalActivity) {
        return new PublicTimelineItemResponse(
                externalActivity.getId(),
                "EXTERNAL_ACTIVITY",
                externalActivity.getOccurredAt(),
                externalActivity.getProject().getSlug(),
                externalActivity.getProject().getName(),
                externalActivity.getTitle(),
                externalActivity.getContentSummary(),
                externalActivity.getActivityType(),
                externalActivity.getOccurredAt().toLocalDate(),
                externalActivity.getOccurredAt(),
                externalActivity.getOccurredAt(),
                false,
                new LinkedHashSet<>()
        );
    }

    private static OffsetDateTime timelineTimeOf(ManualLog manualLog) {
        if (manualLog.getEndedAt() != null) {
            return manualLog.getEndedAt();
        }
        if (manualLog.getStartedAt() != null) {
            return manualLog.getStartedAt();
        }
        return manualLog.getCreatedAt();
    }
}
