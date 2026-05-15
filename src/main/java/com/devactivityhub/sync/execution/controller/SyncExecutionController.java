package com.devactivityhub.sync.execution.controller;

import com.devactivityhub.sync.execution.dto.SyncExecutionResponse;
import com.devactivityhub.sync.execution.service.SyncExecutionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sync/executions")
public class SyncExecutionController {

    private final SyncExecutionQueryService syncExecutionQueryService;

    public SyncExecutionController(SyncExecutionQueryService syncExecutionQueryService) {
        this.syncExecutionQueryService = syncExecutionQueryService;
    }

    @GetMapping
    public List<SyncExecutionResponse> getSyncExecutions() {
        return syncExecutionQueryService.getSyncExecutions();
    }

    @GetMapping("/{executionId}")
    public SyncExecutionResponse getSyncExecution(@PathVariable Long executionId) {
        return syncExecutionQueryService.getSyncExecution(executionId);
    }
}
