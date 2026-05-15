package com.devactivityhub.publicview.stats.dto;

import java.time.LocalDate;
import java.util.List;

public record PublicWeeklyStatsResponse(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        long totalActivities,
        long highlightedActivities,
        List<PublicActivityTypeMetricResponse> activityTypeCounts,
        List<PublicProjectMetricResponse> projectCounts
) {
}
