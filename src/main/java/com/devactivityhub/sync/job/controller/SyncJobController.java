package com.devactivityhub.sync.job.controller;

import com.devactivityhub.sync.github.service.GithubSyncService;
import com.devactivityhub.sync.github.service.GithubSyncSummary;
import com.devactivityhub.sync.github.service.GithubSyncVerificationResult;
import com.devactivityhub.sync.github.service.GithubSyncVerificationService;
import com.devactivityhub.sync.job.dto.SyncJobResponse;
import com.devactivityhub.sync.job.service.SyncJobQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sync/jobs")
public class SyncJobController {

    private final SyncJobQueryService syncJobQueryService;
    private final GithubSyncService githubSyncService;
    private final GithubSyncVerificationService githubSyncVerificationService;

    public SyncJobController(SyncJobQueryService syncJobQueryService,
                             GithubSyncService githubSyncService,
                             GithubSyncVerificationService githubSyncVerificationService) {
        this.syncJobQueryService = syncJobQueryService;
        this.githubSyncService = githubSyncService;
        this.githubSyncVerificationService = githubSyncVerificationService;
    }

    @GetMapping
    public List<SyncJobResponse> getSyncJobs() {
        return syncJobQueryService.getSyncJobs();
    }

    @PostMapping("/{jobId}/run")
    public GithubSyncSummary runSyncJob(@PathVariable Long jobId,
                                        @RequestParam(defaultValue = "MANUAL") String requestedBy,
                                        @RequestParam(defaultValue = "false") boolean fullSync) {
        return githubSyncService.runJob(jobId, requestedBy, fullSync);
    }

    @GetMapping("/{jobId}/verification")
    public GithubSyncVerificationResult verifySyncJob(@PathVariable Long jobId) {
        return githubSyncVerificationService.verifyJob(jobId);
    }
}
