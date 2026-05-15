package com.devactivityhub.sync.execution.domain;

import com.devactivityhub.sync.job.domain.SyncJob;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_executions", schema = "dev_activity_hub")
public class SyncExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_job_id", nullable = false)
    private SyncJob syncJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 20)
    private SyncExecutionStatus executionStatus;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "requested_by", nullable = false, length = 50)
    private String requestedBy;

    @Column(name = "result_summary")
    private String resultSummary;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SyncExecution() {
    }

    public SyncExecution(SyncJob syncJob, String requestedBy) {
        this.syncJob = syncJob;
        this.executionStatus = SyncExecutionStatus.RUNNING;
        this.startedAt = OffsetDateTime.now();
        this.requestedBy = requestedBy;
    }

    public void markSuccess(String resultSummary) {
        this.executionStatus = SyncExecutionStatus.SUCCESS;
        this.finishedAt = OffsetDateTime.now();
        this.resultSummary = resultSummary;
        this.errorMessage = null;
    }

    public void markFailure(String errorMessage) {
        this.executionStatus = SyncExecutionStatus.FAILED;
        this.finishedAt = OffsetDateTime.now();
        this.errorMessage = errorMessage;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SyncJob getSyncJob() {
        return syncJob;
    }

    public SyncExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
