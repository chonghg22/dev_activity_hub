package com.devactivityhub.publicview.stats.dto;

import java.util.List;

public record PublicStatsSummaryResponse(
        long publicProjectCount,
        long totalCommitCount,
        long weeklyCommitCount,
        long totalPullRequestActivityCount,
        long weeklyPullRequestActivityCount,
        long recent7DayActivityCount,
        List<PublicActivityTypeMetricResponse> activityTypeCounts
) {
}
