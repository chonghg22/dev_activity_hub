package com.devactivityhub.publicview.stats.dto;

public record PublicActivityTypeMetricResponse(
        String activityType,
        long count
) {
}
