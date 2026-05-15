package com.devactivityhub.report.weekly.service;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.repository.ManualLogRepository;
import com.devactivityhub.report.weekly.domain.WeeklyReport;
import com.devactivityhub.report.weekly.dto.WeeklyReportResponse;
import com.devactivityhub.report.weekly.repository.WeeklyReportRepository;
import com.devactivityhub.project.domain.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeeklyReportServiceTest {

    private final WeeklyReportRepository weeklyReportRepository = mock(WeeklyReportRepository.class);
    private final ManualLogRepository manualLogRepository = mock(ManualLogRepository.class);
    private final ExternalActivityRepository externalActivityRepository = mock(ExternalActivityRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WeeklyReportService weeklyReportService = new WeeklyReportService(
            weeklyReportRepository, manualLogRepository, externalActivityRepository, objectMapper
    );

    @Test
    void rebuildWeeklyReportCreatesSummaryAndItems() {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(1L);
        when(project.getName()).thenReturn("Dev Activity Hub");

        ManualLog highlightedLog = mock(ManualLog.class);
        when(highlightedLog.getId()).thenReturn(10L);
        when(highlightedLog.getProject()).thenReturn(project);
        when(highlightedLog.getTitle()).thenReturn("Setup project");
        when(highlightedLog.getActivityType()).thenReturn(ManualLogActivityType.WORK_LOG);
        when(highlightedLog.getWorkDate()).thenReturn(LocalDate.of(2026, 5, 12));
        when(highlightedLog.getEndedAt()).thenReturn(OffsetDateTime.parse("2026-05-12T18:00:00+09:00"));
        when(highlightedLog.isHighlighted()).thenReturn(true);
        when(highlightedLog.getTags()).thenReturn(Set.of());
        when(highlightedLog.getCreatedAt()).thenReturn(OffsetDateTime.parse("2026-05-12T18:00:00+09:00"));

        ManualLog normalLog = mock(ManualLog.class);
        when(normalLog.getId()).thenReturn(11L);
        when(normalLog.getProject()).thenReturn(project);
        when(normalLog.getTitle()).thenReturn("Normal work");
        when(normalLog.getActivityType()).thenReturn(ManualLogActivityType.LEARNING);
        when(normalLog.getWorkDate()).thenReturn(LocalDate.of(2026, 5, 13));
        when(normalLog.isHighlighted()).thenReturn(false);
        when(normalLog.getTags()).thenReturn(Set.of());

        ExternalActivity commit = mock(ExternalActivity.class);
        when(commit.getId()).thenReturn(20L);
        when(commit.getProject()).thenReturn(project);
        when(commit.getTitle()).thenReturn("feat: add endpoint");
        when(commit.getActivityType()).thenReturn("COMMIT");
        when(commit.getOccurredAt()).thenReturn(OffsetDateTime.parse("2026-05-14T10:00:00+09:00"));

        when(manualLogRepository.findAll(org.mockito.ArgumentMatchers.<Specification<ManualLog>>any()))
                .thenReturn(List.of(highlightedLog, normalLog));
        when(externalActivityRepository.findAll(org.mockito.ArgumentMatchers.<Specification<ExternalActivity>>any()))
                .thenReturn(List.of(commit));
        when(weeklyReportRepository.findByWeekStartDate(LocalDate.of(2026, 5, 11)))
                .thenReturn(Optional.empty());
        when(weeklyReportRepository.save(any(WeeklyReport.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WeeklyReportResponse response = weeklyReportService.rebuildWeeklyReport(LocalDate.of(2026, 5, 12));

        assertThat(response.weekStartDate()).isEqualTo(LocalDate.of(2026, 5, 11));
        assertThat(response.weekEndDate()).isEqualTo(LocalDate.of(2026, 5, 17));
        assertThat(response.summary().get("totalActivities").asInt()).isEqualTo(3);
        assertThat(response.summary().get("totalManualLogs").asInt()).isEqualTo(2);
        assertThat(response.summary().get("totalExternalActivities").asInt()).isEqualTo(1);
        assertThat(response.summary().get("highlightedCount").asInt()).isEqualTo(1);
        assertThat(response.summary().get("activityTypeCounts").get("WORK_LOG").asInt()).isEqualTo(1);
        assertThat(response.summary().get("activityTypeCounts").get("LEARNING").asInt()).isEqualTo(1);
        assertThat(response.summary().get("activityTypeCounts").get("COMMIT").asInt()).isEqualTo(1);
        assertThat(response.summary().get("projectCounts").get("Dev Activity Hub").asInt()).isEqualTo(3);

        // Items should include highlighted manual log + all external activities
        assertThat(response.items()).hasSize(2);
        assertThat(response.items()).extracting(WeeklyReportResponse.WeeklyReportItemResponse::title)
                .containsExactly("feat: add endpoint", "Setup project");
    }
}
