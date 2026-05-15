package com.devactivityhub.activity.manuallog.dto;

import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record ManualLogUpdateRequest(
        Long projectId,
        @Size(max = 200) String title,
        String content,
        ManualLogActivityType activityType,
        LocalDate workDate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        ManualLogVisibility visibility,
        Boolean isHighlighted,
        Set<@Size(max = 50) String> tags
) {
}
