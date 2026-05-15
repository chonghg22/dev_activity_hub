package com.devactivityhub.activity.manuallog.dto;

import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public record ManualLogCreateRequest(
        @NotNull Long projectId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content,
        @NotNull ManualLogActivityType activityType,
        @NotNull LocalDate workDate,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        ManualLogVisibility visibility,
        Boolean isHighlighted,
        Set<@NotBlank @Size(max = 50) String> tags
) {
}
