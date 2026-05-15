package com.devactivityhub.activity.manuallog.dto;

import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record ManualLogResponse(
        Long id,
        ProjectReferenceResponse project,
        String title,
        String content,
        ManualLogActivityType activityType,
        LocalDate workDate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        ManualLogVisibility visibility,
        boolean isHighlighted,
        Set<String> tags,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ManualLogResponse from(ManualLog manualLog) {
        return new ManualLogResponse(
                manualLog.getId(),
                ProjectReferenceResponse.from(manualLog.getProject()),
                manualLog.getTitle(),
                manualLog.getContent(),
                manualLog.getActivityType(),
                manualLog.getWorkDate(),
                manualLog.getStartedAt(),
                manualLog.getEndedAt(),
                manualLog.getVisibility(),
                manualLog.isHighlighted(),
                manualLog.getTags().stream().map(tag -> tag.getName()).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)),
                manualLog.getCreatedAt(),
                manualLog.getUpdatedAt()
        );
    }
}
