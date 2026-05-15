package com.devactivityhub.publicview.stats.dto;

public record PublicProjectMetricResponse(
        String projectSlug,
        String projectName,
        long count
) {
}
