package com.devactivityhub.sync.job.dto;

import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;

import java.time.OffsetDateTime;

public record SyncJobResponse(
        Long id,
        String sourceType,
        String jobName,
        SyncJobStatus status,
        SyncScheduleType scheduleType,
        OffsetDateTime lastSyncedAt,
        String cursorValue,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SyncJobResponse from(SyncJob syncJob) {
        return new SyncJobResponse(
                syncJob.getId(),
                syncJob.getSourceType(),
                syncJob.getJobName(),
                syncJob.getStatus(),
                syncJob.getScheduleType(),
                syncJob.getLastSyncedAt(),
                syncJob.getCursorValue(),
                syncJob.getCreatedAt(),
                syncJob.getUpdatedAt()
        );
    }
}
