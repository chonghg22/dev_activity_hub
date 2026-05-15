package com.devactivityhub.publicview.stats.service;

import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import com.devactivityhub.activity.manuallog.repository.ManualLogRepository;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.repository.ProjectRepository;
import com.devactivityhub.publicview.stats.dto.PublicActivityTypeMetricResponse;
import com.devactivityhub.publicview.stats.dto.PublicProjectMetricResponse;
import com.devactivityhub.publicview.stats.dto.PublicStatsSummaryResponse;
import com.devactivityhub.publicview.stats.dto.PublicWeeklyStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicStatsQueryService {

    private static final ManualLogVisibility PUBLIC_VISIBILITY = ManualLogVisibility.PUBLIC;
    private static final ProjectStatus ACTIVE_STATUS = ProjectStatus.ACTIVE;

    private final ProjectRepository projectRepository;
    private final ManualLogRepository manualLogRepository;

    public PublicStatsQueryService(ProjectRepository projectRepository,
                                   ManualLogRepository manualLogRepository) {
        this.projectRepository = projectRepository;
        this.manualLogRepository = manualLogRepository;
    }

    public PublicStatsSummaryResponse getSummary() {
        long publicProjectCount = projectRepository.countByIsPublicTrueAndStatus(ACTIVE_STATUS);
        long publicManualLogCount = manualLogRepository.countByVisibilityAndProjectIsPublicTrueAndProjectStatus(
                PUBLIC_VISIBILITY,
                ACTIVE_STATUS
        );
        long highlightedLogCount = manualLogRepository.countByVisibilityAndIsHighlightedTrueAndProjectIsPublicTrueAndProjectStatus(
                PUBLIC_VISIBILITY,
                ACTIVE_STATUS
        );

        List<PublicActivityTypeMetricResponse> activityTypeCounts = manualLogRepository.countPublicLogsByActivityType(
                        PUBLIC_VISIBILITY,
                        ACTIVE_STATUS
                ).stream()
                .map(projection -> new PublicActivityTypeMetricResponse(projection.getActivityType(), projection.getCount()))
                .toList();

        return new PublicStatsSummaryResponse(
                publicProjectCount,
                publicManualLogCount,
                highlightedLogCount,
                activityTypeCounts
        );
    }

    public PublicWeeklyStatsResponse getWeeklyStats(LocalDate requestedWeekStartDate) {
        LocalDate weekStartDate = normalizeWeekStartDate(requestedWeekStartDate);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        long totalActivities = manualLogRepository.countByVisibilityAndProjectIsPublicTrueAndProjectStatusAndWorkDateBetween(
                PUBLIC_VISIBILITY,
                ACTIVE_STATUS,
                weekStartDate,
                weekEndDate
        );
        long highlightedActivities = manualLogRepository.countByVisibilityAndIsHighlightedTrueAndProjectIsPublicTrueAndProjectStatusAndWorkDateBetween(
                PUBLIC_VISIBILITY,
                ACTIVE_STATUS,
                weekStartDate,
                weekEndDate
        );

        List<PublicActivityTypeMetricResponse> activityTypeCounts = manualLogRepository.countPublicLogsByActivityTypeBetween(
                        PUBLIC_VISIBILITY,
                        ACTIVE_STATUS,
                        weekStartDate,
                        weekEndDate
                ).stream()
                .map(projection -> new PublicActivityTypeMetricResponse(projection.getActivityType(), projection.getCount()))
                .toList();

        List<PublicProjectMetricResponse> projectCounts = manualLogRepository.countPublicLogsByProjectBetween(
                        PUBLIC_VISIBILITY,
                        ACTIVE_STATUS,
                        weekStartDate,
                        weekEndDate
                ).stream()
                .map(projection -> new PublicProjectMetricResponse(
                        projection.getProjectSlug(),
                        projection.getProjectName(),
                        projection.getCount()
                ))
                .toList();

        return new PublicWeeklyStatsResponse(
                weekStartDate,
                weekEndDate,
                totalActivities,
                highlightedActivities,
                activityTypeCounts,
                projectCounts
        );
    }

    private LocalDate normalizeWeekStartDate(LocalDate requestedWeekStartDate) {
        LocalDate baseDate = requestedWeekStartDate == null ? LocalDate.now() : requestedWeekStartDate;
        return baseDate.with(DayOfWeek.MONDAY);
    }
}
