package com.devactivityhub.report.weekly.service;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.repository.ManualLogRepository;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.report.weekly.domain.WeeklyReport;
import com.devactivityhub.report.weekly.domain.WeeklyReportItem;
import com.devactivityhub.report.weekly.dto.WeeklyReportResponse;
import com.devactivityhub.report.weekly.repository.WeeklyReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final ManualLogRepository manualLogRepository;
    private final ExternalActivityRepository externalActivityRepository;
    private final ObjectMapper objectMapper;

    public WeeklyReportService(WeeklyReportRepository weeklyReportRepository,
                               ManualLogRepository manualLogRepository,
                               ExternalActivityRepository externalActivityRepository,
                               ObjectMapper objectMapper) {
        this.weeklyReportRepository = weeklyReportRepository;
        this.manualLogRepository = manualLogRepository;
        this.externalActivityRepository = externalActivityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(LocalDate weekStartDate) {
        LocalDate monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyReport report = weeklyReportRepository.findByWeekStartDate(monday)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WeeklyReport not found for week starting " + monday));
        return WeeklyReportResponse.from(report);
    }

    @Transactional
    public WeeklyReportResponse rebuildWeeklyReport(LocalDate weekStartDate) {
        LocalDate monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        OffsetDateTime fromTimestamp = monday.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toTimestamp = sunday.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime now = OffsetDateTime.now();

        List<ManualLog> manualLogs = manualLogRepository.findAll(manualLogInWeek(monday, sunday));
        List<ExternalActivity> externalActivities = externalActivityRepository.findAll(
                externalActivityInWeek(fromTimestamp, toTimestamp));

        JsonNode summaryJson = buildSummary(manualLogs, externalActivities);
        List<WeeklyReportItem> items = buildItems(manualLogs, externalActivities);

        WeeklyReport report = weeklyReportRepository.findByWeekStartDate(monday)
                .orElse(null);

        if (report != null) {
            report.rebuild(now, summaryJson, items);
        } else {
            report = new WeeklyReport(monday, sunday, now, summaryJson);
            for (WeeklyReportItem item : items) {
                report.addItem(item);
            }
            weeklyReportRepository.save(report);
        }

        return WeeklyReportResponse.from(report);
    }

    private JsonNode buildSummary(List<ManualLog> manualLogs, List<ExternalActivity> externalActivities) {
        ObjectNode summary = objectMapper.createObjectNode();

        int totalManualLogs = manualLogs.size();
        int totalExternalActivities = externalActivities.size();
        int totalActivities = totalManualLogs + totalExternalActivities;

        summary.put("totalActivities", totalActivities);
        summary.put("totalManualLogs", totalManualLogs);
        summary.put("totalExternalActivities", totalExternalActivities);

        long highlightedCount = manualLogs.stream().filter(ManualLog::isHighlighted).count();
        summary.put("highlightedCount", highlightedCount);

        // Activity type counts
        Map<String, Integer> activityTypeCounts = new LinkedHashMap<>();
        for (ManualLog ml : manualLogs) {
            activityTypeCounts.merge(ml.getActivityType().name(), 1, Integer::sum);
        }
        for (ExternalActivity ea : externalActivities) {
            activityTypeCounts.merge(ea.getActivityType(), 1, Integer::sum);
        }
        ObjectNode activityTypeNode = objectMapper.createObjectNode();
        activityTypeCounts.forEach(activityTypeNode::put);
        summary.set("activityTypeCounts", activityTypeNode);

        // Project counts
        Map<String, Integer> projectCounts = new LinkedHashMap<>();
        for (ManualLog ml : manualLogs) {
            projectCounts.merge(ml.getProject().getName(), 1, Integer::sum);
        }
        for (ExternalActivity ea : externalActivities) {
            if (ea.getProject() != null) {
                projectCounts.merge(ea.getProject().getName(), 1, Integer::sum);
            }
        }
        ObjectNode projectNode = objectMapper.createObjectNode();
        projectCounts.forEach(projectNode::put);
        summary.set("projectCounts", projectNode);

        return summary;
    }

    private List<WeeklyReportItem> buildItems(List<ManualLog> manualLogs, List<ExternalActivity> externalActivities) {
        List<WeeklyReportItem> items = new ArrayList<>();

        for (ManualLog ml : manualLogs) {
            if (ml.isHighlighted()) {
                OffsetDateTime occurredAt = ml.getEndedAt() != null ? ml.getEndedAt()
                        : ml.getStartedAt() != null ? ml.getStartedAt()
                        : ml.getCreatedAt();
                items.add(new WeeklyReportItem(
                        "MANUAL_LOG", ml.getId(), ml.getProject(),
                        ml.getActivityType().name(), occurredAt, ml.getTitle()
                ));
            }
        }

        for (ExternalActivity ea : externalActivities) {
            items.add(new WeeklyReportItem(
                    "EXTERNAL_ACTIVITY", ea.getId(), ea.getProject(),
                    ea.getActivityType(), ea.getOccurredAt(), ea.getTitle()
            ));
        }

        items.sort((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()));
        return items;
    }

    private Specification<ManualLog> manualLogInWeek(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("workDate"), from),
                cb.lessThanOrEqualTo(root.get("workDate"), to)
        );
    }

    private Specification<ExternalActivity> externalActivityInWeek(OffsetDateTime from, OffsetDateTime to) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("occurredAt"), from),
                cb.lessThan(root.get("occurredAt"), to)
        );
    }
}
