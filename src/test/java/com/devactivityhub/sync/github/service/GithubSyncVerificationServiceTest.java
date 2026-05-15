package com.devactivityhub.sync.github.service;

import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.github.client.GithubApiClient;
import com.devactivityhub.sync.github.client.GithubApiResponse;
import com.devactivityhub.sync.github.client.GithubRateLimitSnapshot;
import com.devactivityhub.sync.github.config.GithubSyncProperties;
import com.devactivityhub.sync.github.dto.GithubRepositoryResponse;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubSyncVerificationServiceTest {

    @Mock
    private SyncJobRepository syncJobRepository;

    @Mock
    private ProjectSourceRepository projectSourceRepository;

    @Mock
    private GithubApiClient githubApiClient;

    private GithubSyncVerificationService githubSyncVerificationService;

    @BeforeEach
    void setUp() {
        githubSyncVerificationService = new GithubSyncVerificationService(
                syncJobRepository,
                projectSourceRepository,
                githubApiClient,
                new GithubSyncProperties("https://api.github.com", "", 100, 10, 5)
        );
    }

    @Test
    void verifyJobReturnsRepositoryVerificationResult() {
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

        when(syncJobRepository.findById(9L)).thenReturn(Optional.of(syncJob));
        when(projectSourceRepository.findBySourceTypeAndExternalName(ProjectSourceType.GITHUB, "chonghg22/dev_activity_hub"))
                .thenReturn(Optional.of(projectSource));
        when(githubApiClient.fetchRepository("chonghg22", "dev_activity_hub"))
                .thenReturn(new GithubApiResponse<>(
                        new GithubRepositoryResponse(
                                101L,
                                "chonghg22/dev_activity_hub",
                                "https://github.com/chonghg22/dev_activity_hub",
                                false,
                                "main",
                                OffsetDateTime.parse("2026-05-15T02:02:11Z")
                        ),
                        new GithubRateLimitSnapshot(60, 57, OffsetDateTime.parse("2026-05-15T03:00:00Z"))
                ));

        GithubSyncVerificationResult result = githubSyncVerificationService.verifyJob(9L);

        assertThat(result.jobId()).isEqualTo(9L);
        assertThat(result.repository()).isEqualTo("chonghg22/dev_activity_hub");
        assertThat(result.tokenConfigured()).isFalse();
        assertThat(result.repositoryAccessible()).isTrue();
        assertThat(result.defaultBranch()).isEqualTo("main");
        assertThat(result.rateLimitRemaining()).isEqualTo(57);
    }
}
