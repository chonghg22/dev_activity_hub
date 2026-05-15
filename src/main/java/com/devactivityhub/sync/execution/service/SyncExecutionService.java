package com.devactivityhub.sync.execution.service;

import com.devactivityhub.sync.execution.domain.SyncExecution;
import com.devactivityhub.sync.execution.repository.SyncExecutionRepository;
import com.devactivityhub.sync.job.domain.SyncJob;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SyncExecutionService {

    private final SyncExecutionRepository syncExecutionRepository;

    public SyncExecutionService(SyncExecutionRepository syncExecutionRepository) {
        this.syncExecutionRepository = syncExecutionRepository;
    }

    public SyncExecution start(SyncJob syncJob, String requestedBy) {
        return syncExecutionRepository.save(new SyncExecution(syncJob, requestedBy));
    }

    public void markSuccess(SyncExecution syncExecution, String resultSummary) {
        syncExecution.markSuccess(resultSummary);
    }

    public void markFailure(SyncExecution syncExecution, String errorMessage) {
        syncExecution.markFailure(errorMessage);
    }
}
