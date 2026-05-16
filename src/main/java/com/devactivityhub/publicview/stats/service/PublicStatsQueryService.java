package com.devactivityhub.publicview.stats.service;

import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PublicStatsQueryService {

    private static final ManualLogVisibility PUBLIC_VISIBILITY = ManualLogVisibility.PUBLIC;
    private static final ProjectStatus ACTIVE_STATUS = ProjectStatus.ACTIVE;
    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String COMMIT_ACTIVITY = "COMMIT";
    private static final List<String> PULL_REQUEST_ACTIVITIES = List.of("PR_OPENED", "PR_MERGED", "PR_CLOSED");

    private final ProjectRepository projectRepository;
    private final ManualLogRepository manualLogRepository;
    private final ExternalActivityRepository externalActivityRepository;

    public PublicStatsQueryService(ProjectRepository projectRepository,
                                   ManualLogRepository manualLogRepository,
                                   ExternalActivityRepository externalActivityRepository) {
        this.projectRepository = projectRepository;
        this.manualLogRepository = manualLogRepository;
        this.externalActivityRepository = externalActivityRepository;
    }

    public PublicStatsSummaryResponse getSummary() {
        long publicProjectCount = projectRepository.countByIsPublicTrueAndStatus(ACTIVE_STATUS);
        LocalDate today = LocalDate.now(APP_ZONE_ID);
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        OffsetDateTime weekStart = weekStartDate.atStartOfDay(APP_ZONE_ID).toOffsetDateTime();
        OffsetDateTime nextWeekStart = weekStartDate.plusDays(7).atStartOfDay(APP_ZONE_ID).toOffsetDateTime();
        OffsetDateTime recent7DayStart = today.minusDays(6).atStartOfDay(APP_ZONE_ID).toOffsetDateTime();
        OffsetDateTime tomorrowStart = today.plusDays(1).atStartOfDay(APP_ZONE_ID).toOffsetDateTime();

        long totalCommitCount = externalActivityRepository.countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityType(
                ACTIVE_STATUS,
                COMMIT_ACTIVITY
        );
        long weeklyCommitCount = externalActivityRepository.countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                ACTIVE_STATUS,
                COMMIT_ACTIVITY,
                weekStart,
                nextWeekStart
        );
        long totalPullRequestActivityCount = externalActivityRepository.countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeIn(
                ACTIVE_STATUS,
                PULL_REQUEST_ACTIVITIES
        );
        long weeklyPullRequestActivityCount = externalActivityRepository.countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeInAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                ACTIVE_STATUS,
                PULL_REQUEST_ACTIVITIES,
                weekStart,
                nextWeekStart
        );
        long recent7DayManualLogCount = manualLogRepository.countByVisibilityAndProjectIsPublicTrueAndProjectStatusAndWorkDateBetween(
                PUBLIC_VISIBILITY,
                ACTIVE_STATUS,
                today.minusDays(6),
                today
        );
        long recent7DayExternalActivityCount = externalActivityRepository.countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
                ACTIVE_STATUS,
                recent7DayStart,
                tomorrowStart
        );
        long recent7DayActivityCount = recent7DayManualLogCount + recent7DayExternalActivityCount;

        Map<String, Long> activityTypeCountMap = new LinkedHashMap<>();
        manualLogRepository.countPublicLogsByActivityType(PUBLIC_VISIBILITY, ACTIVE_STATUS)
                .forEach(projection -> activityTypeCountMap.merge(projection.getActivityType(), projection.getCount(), Long::sum));
        externalActivityRepository.countPublicActivitiesByActivityType(ACTIVE_STATUS)
                .forEach(projection -> activityTypeCountMap.merge(projection.getActivityType(), projection.getCount(), Long::sum));

        List<PublicActivityTypeMetricResponse> activityTypeCounts = activityTypeCountMap.entrySet().stream()
                .sorted((left, right) -> {
                    int countCompare = Long.compare(right.getValue(), left.getValue());
                    return countCompare != 0 ? countCompare : left.getKey().compareTo(right.getKey());
                })
                .map(entry -> new PublicActivityTypeMetricResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new PublicStatsSummaryResponse(
                publicProjectCount,
                totalCommitCount,
                weeklyCommitCount,
                totalPullRequestActivityCount,
                weeklyPullRequestActivityCount,
                recent7DayActivityCount,
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
        LocalDate baseDate = requestedWeekStartDate == null ? LocalDate.now(APP_ZONE_ID) : requestedWeekStartDate;
        return baseDate.with(DayOfWeek.MONDAY);
    }
}
