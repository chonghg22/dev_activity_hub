package com.devactivityhub.sync.execution.dto;

import com.devactivityhub.sync.execution.domain.SyncExecution;
import com.devactivityhub.sync.execution.domain.SyncExecutionStatus;

import java.time.OffsetDateTime;

public record SyncExecutionResponse(
        Long id,
        Long syncJobId,
        SyncExecutionStatus executionStatus,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String requestedBy,
        String resultSummary,
        String errorMessage,
        OffsetDateTime createdAt
) {
    public static SyncExecutionResponse from(SyncExecution syncExecution) {
        return new SyncExecutionResponse(
                syncExecution.getId(),
                syncExecution.getSyncJob().getId(),
                syncExecution.getExecutionStatus(),
                syncExecution.getStartedAt(),
                syncExecution.getFinishedAt(),
                syncExecution.getRequestedBy(),
                syncExecution.getResultSummary(),
                syncExecution.getErrorMessage(),
                syncExecution.getCreatedAt()
        );
    }
}
