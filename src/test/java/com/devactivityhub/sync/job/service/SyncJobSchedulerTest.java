package com.devactivityhub.sync.job.service;

import com.devactivityhub.sync.github.service.GithubSyncService;
import com.devactivityhub.sync.job.config.SyncSchedulerProperties;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncJobSchedulerTest {

    @Mock
    private SyncJobRepository syncJobRepository;

    @Mock
    private SyncJobService syncJobService;

    @Mock
    private GithubSyncService githubSyncService;

    @Test
    void runScheduledJobsRunsReadyHourlyGithubJobWhenDue() {
        SyncJobScheduler syncJobScheduler = new SyncJobScheduler(
                syncJobRepository,
                syncJobService,
                githubSyncService,
                new SyncSchedulerProperties(true, 300_000L, 60, true)
        );
        SyncJob job = new SyncJob("GITHUB", "github:sample/repo", SyncJobStatus.READY, SyncScheduleType.HOURLY);
        ReflectionTestUtils.setField(job, "id", 9L);

        when(syncJobRepository.findAllBySourceType("GITHUB")).thenReturn(List.of(job));

        syncJobScheduler.runScheduledJobs();

        verify(syncJobService).upgradeLegacyGithubJobsToHourly();
        verify(githubSyncService).runJob(9L, "SCHEDULER", false);
    }

    @Test
    void runScheduledJobsSkipsJobThatIsNotDueYet() {
        SyncJobScheduler syncJobScheduler = new SyncJobScheduler(
                syncJobRepository,
                syncJobService,
                githubSyncService,
                new SyncSchedulerProperties(true, 300_000L, 60, true)
        );
        SyncJob job = new SyncJob("GITHUB", "github:sample/repo", SyncJobStatus.READY, SyncScheduleType.HOURLY);
        ReflectionTestUtils.setField(job, "id", 9L);
        ReflectionTestUtils.setField(job, "lastSyncedAt", OffsetDateTime.now().minusMinutes(10));

        when(syncJobRepository.findAllBySourceType("GITHUB")).thenReturn(List.of(job));

        syncJobScheduler.runScheduledJobs();

        verify(githubSyncService, never()).runJob(9L, "SCHEDULER", false);
    }
}
