package com.devactivityhub.sync.github.service;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.execution.domain.SyncExecution;
import com.devactivityhub.sync.execution.service.SyncExecutionService;
import com.devactivityhub.sync.github.client.GithubApiClient;
import com.devactivityhub.sync.github.client.GithubFetchResult;
import com.devactivityhub.sync.github.client.GithubRateLimitSnapshot;
import com.devactivityhub.sync.github.config.GithubSyncProperties;
import com.devactivityhub.sync.github.dto.GithubCommitResponse;
import com.devactivityhub.sync.github.dto.GithubIssueResponse;
import com.devactivityhub.sync.github.dto.GithubPullRequestResponse;
import com.devactivityhub.sync.github.dto.GithubUserResponse;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubSyncServiceTest {

    @Mock
    private SyncJobRepository syncJobRepository;

    @Mock
    private ProjectSourceRepository projectSourceRepository;

    @Mock
    private ExternalActivityRepository externalActivityRepository;

    @Mock
    private SyncExecutionService syncExecutionService;

    @Mock
    private GithubApiClient githubApiClient;

    private GithubSyncService githubSyncService;

    @BeforeEach
    void setUp() {
        githubSyncService = new GithubSyncService(
                syncJobRepository,
                projectSourceRepository,
                externalActivityRepository,
                syncExecutionService,
                githubApiClient,
                new GithubSyncProperties("https://api.github.com", "", 100, 10, 5),
                new ObjectMapper().findAndRegisterModules()
        );
    }

    @Test
    void runJobSavesGithubActivitiesAndMarksJobSuccess() {
        Project project = new Project(
                "Dev Activity Hub",
                "dev-activity-hub",
                "portfolio backend",
                "PORTFOLIO",
                ProjectVisibility.PUBLIC,
                true,
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 15),
                null
        );
        ReflectionTestUtils.setField(project, "id", 1L);

        ProjectSource projectSource = new ProjectSource(
                project,
                ProjectSourceType.GITHUB,
                "101",
                "chonghg22/dev_activity_hub",
                "https://github.com/chonghg22/dev_activity_hub",
                true
        );
        ReflectionTestUtils.setField(projectSource, "id", 5L);

        SyncJob syncJob = new SyncJob("GITHUB", "github:chonghg22/dev_activity_hub", SyncJobStatus.READY, SyncScheduleType.MANUAL);
        ReflectionTestUtils.setField(syncJob, "id", 9L);

        SyncExecution syncExecution = new SyncExecution(syncJob, "MANUAL");
        ReflectionTestUtils.setField(syncExecution, "id", 15L);

        when(syncJobRepository.findById(9L)).thenReturn(Optional.of(syncJob));
        when(projectSourceRepository.findBySourceTypeAndExternalName(ProjectSourceType.GITHUB, "chonghg22/dev_activity_hub"))
                .thenReturn(Optional.of(projectSource));
        when(syncExecutionService.start(syncJob, "MANUAL")).thenReturn(syncExecution);
        when(githubApiClient.fetchCommits(eq("chonghg22"), eq("dev_activity_hub"), any()))
                .thenReturn(new GithubFetchResult<>(List.of(sampleCommit()), new GithubRateLimitSnapshot(5000, 4999, null)));
        when(githubApiClient.fetchIssues(eq("chonghg22"), eq("dev_activity_hub"), any()))
                .thenReturn(new GithubFetchResult<>(List.of(sampleIssue()), new GithubRateLimitSnapshot(5000, 4998, null)));
        when(githubApiClient.fetchPullRequests(eq("chonghg22"), eq("dev_activity_hub"), any()))
                .thenReturn(new GithubFetchResult<>(List.of(samplePullRequest()), new GithubRateLimitSnapshot(5000, 4997, null)));
        when(externalActivityRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(Optional.empty());
        when(externalActivityRepository.save(any(ExternalActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GithubSyncSummary summary = githubSyncService.runJob(9L, "MANUAL");

        assertThat(summary.totalSaved()).isEqualTo(5);
        assertThat(summary.commitsSaved()).isEqualTo(1);
        assertThat(summary.issueActivitiesSaved()).isEqualTo(2);
        assertThat(summary.pullRequestActivitiesSaved()).isEqualTo(2);
        assertThat(syncJob.getStatus()).isEqualTo(SyncJobStatus.READY);
        assertThat(syncJob.getCursorValue()).isNotBlank();
        verify(syncExecutionService).markSuccess(eq(syncExecution), any());
    }

    @Test
    void runJobUsesLatestActivityTimestampAsCursorForFullSync() {
        Project project = new Project(
                "Dev Activity Hub",
                "dev-activity-hub",
                "portfolio backend",
                "PORTFOLIO",
                ProjectVisibility.PUBLIC,
                true,
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 15),
                null
        );
        ReflectionTestUtils.setField(project, "id", 1L);

        ProjectSource projectSource = new ProjectSource(
                project,
                ProjectSourceType.GITHUB,
                "101",
                "chonghg22/dev_activity_hub",
                "https://github.com/chonghg22/dev_activity_hub",
                true
        );
        ReflectionTestUtils.setField(projectSource, "id", 5L);

        SyncJob syncJob = new SyncJob("GITHUB", "github:chonghg22/dev_activity_hub", SyncJobStatus.READY, SyncScheduleType.MANUAL);
        ReflectionTestUtils.setField(syncJob, "id", 9L);

        SyncExecution syncExecution = new SyncExecution(syncJob, "MANUAL");
        ReflectionTestUtils.setField(syncExecution, "id", 15L);

        when(syncJobRepository.findById(9L)).thenReturn(Optional.of(syncJob));
        when(projectSourceRepository.findBySourceTypeAndExternalName(ProjectSourceType.GITHUB, "chonghg22/dev_activity_hub"))
                .thenReturn(Optional.of(projectSource));
        when(syncExecutionService.start(syncJob, "MANUAL")).thenReturn(syncExecution);
        when(githubApiClient.fetchCommits(eq("chonghg22"), eq("dev_activity_hub"), eq(null)))
                .thenReturn(new GithubFetchResult<>(List.of(sampleCommit()), new GithubRateLimitSnapshot(5000, 4999, null)));
        when(githubApiClient.fetchIssues(eq("chonghg22"), eq("dev_activity_hub"), eq(null)))
                .thenReturn(new GithubFetchResult<>(List.of(sampleIssue()), new GithubRateLimitSnapshot(5000, 4998, null)));
        when(githubApiClient.fetchPullRequests(eq("chonghg22"), eq("dev_activity_hub"), eq(null)))
                .thenReturn(new GithubFetchResult<>(List.of(samplePullRequest()), new GithubRateLimitSnapshot(5000, 4997, null)));
        when(externalActivityRepository.findBySourceTypeAndSourceId(any(), any())).thenReturn(Optional.empty());
        when(externalActivityRepository.save(any(ExternalActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GithubSyncSummary summary = githubSyncService.runJob(9L, "MANUAL", true);

        assertThat(summary.cursorValue()).isEqualTo("2026-05-15T10:00Z");
        assertThat(syncJob.getCursorValue()).isEqualTo("2026-05-15T10:00Z");
    }

    private GithubCommitResponse sampleCommit() {
        return new GithubCommitResponse(
                "abc123",
                new GithubUserResponse("chonghg22", "https://github.com/chonghg22"),
                new GithubCommitResponse.GithubCommitDetail(
                        new GithubCommitResponse.GithubCommitAuthor(
                                "chonghg22",
                                "dev@example.com",
                                OffsetDateTime.parse("2026-05-15T10:00:00Z")
                        ),
                        "Implement sync service\n\nDetails"
                ),
                "https://github.com/chonghg22/dev_activity_hub/commit/abc123"
        );
    }

    private GithubIssueResponse sampleIssue() {
        return new GithubIssueResponse(
                201L,
                12,
                "Add sync endpoint",
                "Need manual trigger.",
                new GithubUserResponse("chonghg22", "https://github.com/chonghg22"),
                "https://github.com/chonghg22/dev_activity_hub/issues/12",
                OffsetDateTime.parse("2026-05-14T08:00:00Z"),
                OffsetDateTime.parse("2026-05-15T08:00:00Z"),
                null
        );
    }

    private GithubPullRequestResponse samplePullRequest() {
        return new GithubPullRequestResponse(
                301L,
                21,
                "Merge github sync",
                "Implements collection path.",
                new GithubUserResponse("chonghg22", "https://github.com/chonghg22"),
                "https://github.com/chonghg22/dev_activity_hub/pull/21",
                OffsetDateTime.parse("2026-05-14T09:00:00Z"),
                OffsetDateTime.parse("2026-05-15T09:00:00Z"),
                OffsetDateTime.parse("2026-05-15T09:00:00Z")
        );
    }
}
