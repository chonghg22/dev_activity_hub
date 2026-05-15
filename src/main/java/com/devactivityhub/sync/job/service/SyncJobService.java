package com.devactivityhub.sync.job.service;

import com.devactivityhub.common.error.DuplicateResourceException;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class SyncJobService {

    private final SyncJobRepository syncJobRepository;

    public SyncJobService(SyncJobRepository syncJobRepository) {
        this.syncJobRepository = syncJobRepository;
    }

    public void ensureSyncJob(ProjectSource projectSource) {
        String sourceType = projectSource.getSourceType().name();
        String jobName = buildJobName(projectSource);

        syncJobRepository.findBySourceTypeAndJobName(sourceType, jobName)
                .ifPresentOrElse(
                        existing -> existing.updateStatus(SyncJobStatus.READY),
                        () -> syncJobRepository.save(new SyncJob(
                                sourceType,
                                jobName,
                                SyncJobStatus.READY,
                                SyncScheduleType.MANUAL
                        ))
                );
    }

    public void syncJobName(ProjectSource projectSource, String previousJobName) {
        String sourceType = projectSource.getSourceType().name();
        String nextJobName = buildJobName(projectSource);

        if (previousJobName.equals(nextJobName)) {
            ensureSyncJob(projectSource);
            return;
        }

        syncJobRepository.findBySourceTypeAndJobName(sourceType, nextJobName)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Sync job already exists. sourceType=" + sourceType + ", jobName=" + nextJobName);
                });

        syncJobRepository.findBySourceTypeAndJobName(sourceType, previousJobName)
                .ifPresentOrElse(
                        existing -> {
                            existing.updateJobName(nextJobName);
                            existing.updateStatus(SyncJobStatus.READY);
                        },
                        () -> ensureSyncJob(projectSource)
                );
    }

    public void deleteSyncJob(ProjectSource projectSource) {
        String sourceType = projectSource.getSourceType().name();
        String jobName = buildJobName(projectSource);
        syncJobRepository.findBySourceTypeAndJobName(sourceType, jobName)
                .ifPresent(syncJobRepository::delete);
    }

    private String buildJobName(ProjectSource projectSource) {
        return switch (projectSource.getSourceType()) {
            case GITHUB -> "github:" + projectSource.getExternalName().trim().toLowerCase(Locale.ROOT);
        };
    }
}
