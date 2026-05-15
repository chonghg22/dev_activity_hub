package com.devactivityhub.sync.job.service;

import com.devactivityhub.sync.github.service.GithubSyncService;
import com.devactivityhub.sync.job.config.SyncSchedulerProperties;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class SyncJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(SyncJobScheduler.class);
    private static final String GITHUB_SOURCE_TYPE = "GITHUB";
    private static final String REQUESTED_BY_SCHEDULER = "SCHEDULER";

    private final SyncJobRepository syncJobRepository;
    private final SyncJobService syncJobService;
    private final GithubSyncService githubSyncService;
    private final SyncSchedulerProperties syncSchedulerProperties;

    public SyncJobScheduler(SyncJobRepository syncJobRepository,
                            SyncJobService syncJobService,
                            GithubSyncService githubSyncService,
                            SyncSchedulerProperties syncSchedulerProperties) {
        this.syncJobRepository = syncJobRepository;
        this.syncJobService = syncJobService;
        this.githubSyncService = githubSyncService;
        this.syncSchedulerProperties = syncSchedulerProperties;
    }

    @Scheduled(fixedDelayString = "${app.sync.scheduler.fixed-delay-ms:300000}")
    public void runScheduledJobs() {
        if (!syncSchedulerProperties.resolvedEnabled()) {
            return;
        }

        syncJobService.upgradeLegacyGithubJobsToHourly();

        List<SyncJob> githubJobs = syncJobRepository.findAllBySourceType(GITHUB_SOURCE_TYPE);
        OffsetDateTime now = OffsetDateTime.now();

        githubJobs.stream()
                .filter(this::isSchedulable)
                .filter(job -> isDue(job, now))
                .forEach(this::runGithubJobSafely);
    }

    private boolean isSchedulable(SyncJob job) {
        if (job.getScheduleType() != SyncScheduleType.HOURLY) {
            return false;
        }
        return job.getStatus() == SyncJobStatus.READY || job.getStatus() == SyncJobStatus.FAILED;
    }

    private boolean isDue(SyncJob job, OffsetDateTime now) {
        if (job.getLastSyncedAt() == null) {
            return true;
        }
        return !job.getLastSyncedAt()
                .plusMinutes(syncSchedulerProperties.resolvedHourlyIntervalMinutes())
                .isAfter(now);
    }

    private void runGithubJobSafely(SyncJob job) {
        try {
            githubSyncService.runJob(job.getId(), REQUESTED_BY_SCHEDULER, false);
        } catch (RuntimeException exception) {
            log.warn("Scheduled sync job failed. jobId={}, jobName={}, message={}",
                    job.getId(), job.getJobName(), exception.getMessage());
        }
    }
}
