package com.devactivityhub.sync.job.domain;

import com.devactivityhub.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_jobs", schema = "dev_activity_hub")
public class SyncJob extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SyncJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false, length = 20)
    private SyncScheduleType scheduleType;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    @Column(name = "cursor_value", length = 255)
    private String cursorValue;

    protected SyncJob() {
    }

    public SyncJob(String sourceType,
                   String jobName,
                   SyncJobStatus status,
                   SyncScheduleType scheduleType) {
        this.sourceType = sourceType;
        this.jobName = jobName;
        this.status = status;
        this.scheduleType = scheduleType;
    }

    public void markRunning() {
        this.status = SyncJobStatus.RUNNING;
    }

    public void markSuccess(OffsetDateTime syncedAt, String cursorValue) {
        this.status = SyncJobStatus.READY;
        this.lastSyncedAt = syncedAt;
        this.cursorValue = cursorValue;
    }

    public void markFailure() {
        this.status = SyncJobStatus.FAILED;
    }

    public void updateJobName(String jobName) {
        this.jobName = jobName;
    }

    public void updateStatus(SyncJobStatus status) {
        this.status = status;
    }

    public void updateScheduleType(SyncScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Long getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getJobName() {
        return jobName;
    }

    public SyncJobStatus getStatus() {
        return status;
    }

    public SyncScheduleType getScheduleType() {
        return scheduleType;
    }

    public OffsetDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public String getCursorValue() {
        return cursorValue;
    }
}
