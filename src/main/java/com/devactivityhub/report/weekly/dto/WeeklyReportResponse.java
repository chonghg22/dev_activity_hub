package com.devactivityhub.report.weekly.dto;

import com.devactivityhub.report.weekly.domain.WeeklyReport;
import com.devactivityhub.report.weekly.domain.WeeklyReportItem;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record WeeklyReportResponse(
        Long id,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        OffsetDateTime generatedAt,
        JsonNode summary,
        List<WeeklyReportItemResponse> items
) {
    public static WeeklyReportResponse from(WeeklyReport report) {
        return new WeeklyReportResponse(
                report.getId(),
                report.getWeekStartDate(),
                report.getWeekEndDate(),
                report.getGeneratedAt(),
                report.getSummaryJson(),
                report.getItems().stream()
                        .map(WeeklyReportItemResponse::from)
                        .toList()
        );
    }

    public record WeeklyReportItemResponse(
            Long id,
            String sourceKind,
            Long sourceRefId,
            Long projectId,
            String projectName,
            String activityType,
            OffsetDateTime occurredAt,
            String title
    ) {
        public static WeeklyReportItemResponse from(WeeklyReportItem item) {
            return new WeeklyReportItemResponse(
                    item.getId(),
                    item.getSourceKind(),
                    item.getSourceRefId(),
                    item.getProject() != null ? item.getProject().getId() : null,
                    item.getProject() != null ? item.getProject().getName() : null,
                    item.getActivityType(),
                    item.getOccurredAt(),
                    item.getTitle()
            );
        }
    }
}
