package com.devactivityhub.sync.execution.service;

import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.sync.execution.dto.SyncExecutionResponse;
import com.devactivityhub.sync.execution.repository.SyncExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SyncExecutionQueryService {

    private final SyncExecutionRepository syncExecutionRepository;

    public SyncExecutionQueryService(SyncExecutionRepository syncExecutionRepository) {
        this.syncExecutionRepository = syncExecutionRepository;
    }

    public List<SyncExecutionResponse> getSyncExecutions() {
        return syncExecutionRepository.findAll().stream()
                .map(SyncExecutionResponse::from)
                .toList();
    }

    public SyncExecutionResponse getSyncExecution(Long executionId) {
        return syncExecutionRepository.findById(executionId)
                .map(SyncExecutionResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Sync execution not found. id=" + executionId));
    }
}
