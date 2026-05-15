package com.devactivityhub.sync.job.controller;

import com.devactivityhub.sync.github.service.GithubSyncService;
import com.devactivityhub.sync.github.service.GithubSyncSummary;
import com.devactivityhub.sync.github.service.GithubSyncVerificationResult;
import com.devactivityhub.sync.github.service.GithubSyncVerificationService;
import com.devactivityhub.sync.job.domain.SyncJobStatus;
import com.devactivityhub.sync.job.domain.SyncScheduleType;
import com.devactivityhub.sync.job.dto.SyncJobResponse;
import com.devactivityhub.sync.job.service.SyncJobQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncJobController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class SyncJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SyncJobQueryService syncJobQueryService;

    @MockBean
    private GithubSyncService githubSyncService;

    @MockBean
    private GithubSyncVerificationService githubSyncVerificationService;

    @Test
    void getSyncJobsReturnsList() throws Exception {
        when(syncJobQueryService.getSyncJobs()).thenReturn(List.of(new SyncJobResponse(
                9L,
                "GITHUB",
                "github:chonghg22/dev_activity_hub",
                SyncJobStatus.READY,
                SyncScheduleType.MANUAL,
                null,
                null,
                OffsetDateTime.parse("2026-05-15T14:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T14:00:00+09:00")
        )));

        mockMvc.perform(get("/api/sync/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobName").value("github:chonghg22/dev_activity_hub"));
    }

    @Test
    void runSyncJobReturnsSummary() throws Exception {
        when(githubSyncService.runJob(eq(9L), eq("MANUAL"), eq(false))).thenReturn(new GithubSyncSummary(
                9L,
                15L,
                1,
                2,
                2,
                5,
                "2026-05-15T15:00:00+09:00"
        ));

        mockMvc.perform(post("/api/sync/jobs/9/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSaved").value(5))
                .andExpect(jsonPath("$.executionId").value(15));
    }

    @Test
    void runSyncJobSupportsFullSyncFlag() throws Exception {
        when(githubSyncService.runJob(eq(9L), eq("MANUAL"), eq(true))).thenReturn(new GithubSyncSummary(
                9L,
                15L,
                0,
                0,
                0,
                0,
                "2026-05-15T15:00:00+09:00"
        ));

        mockMvc.perform(post("/api/sync/jobs/9/run").param("fullSync", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSaved").value(0))
                .andExpect(jsonPath("$.executionId").value(15));
    }

    @Test
    void verifySyncJobReturnsVerificationResult() throws Exception {
        when(githubSyncVerificationService.verifyJob(9L)).thenReturn(new GithubSyncVerificationResult(
                9L,
                "chonghg22/dev_activity_hub",
                false,
                true,
                "https://github.com/chonghg22/dev_activity_hub",
                false,
                "main",
                OffsetDateTime.parse("2026-05-15T02:02:11Z"),
                57,
                OffsetDateTime.parse("2026-05-15T03:00:00Z")
        ));

        mockMvc.perform(get("/api/sync/jobs/9/verification"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repository").value("chonghg22/dev_activity_hub"))
                .andExpect(jsonPath("$.repositoryAccessible").value(true))
                .andExpect(jsonPath("$.tokenConfigured").value(false));
    }
}
