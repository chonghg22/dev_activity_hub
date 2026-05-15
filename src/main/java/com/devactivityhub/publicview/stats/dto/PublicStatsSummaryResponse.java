package com.devactivityhub.publicview.stats.dto;

import java.util.List;

public record PublicStatsSummaryResponse(
        long publicProjectCount,
        long publicManualLogCount,
        long highlightedLogCount,
        List<PublicActivityTypeMetricResponse> activityTypeCounts
) {
}
