package com.devactivityhub.sync.execution.controller;

import com.devactivityhub.sync.execution.domain.SyncExecutionStatus;
import com.devactivityhub.sync.execution.dto.SyncExecutionResponse;
import com.devactivityhub.sync.execution.service.SyncExecutionQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SyncExecutionController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class SyncExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SyncExecutionQueryService syncExecutionQueryService;

    @Test
    void getSyncExecutionsReturnsList() throws Exception {
        when(syncExecutionQueryService.getSyncExecutions()).thenReturn(List.of(new SyncExecutionResponse(
                15L,
                9L,
                SyncExecutionStatus.SUCCESS,
                OffsetDateTime.parse("2026-05-15T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T15:01:00+09:00"),
                "MANUAL",
                "Saved 5 activities",
                null,
                OffsetDateTime.parse("2026-05-15T15:00:00+09:00")
        )));

        mockMvc.perform(get("/api/sync/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].executionStatus").value("SUCCESS"));
    }

    @Test
    void getSyncExecutionReturnsDetail() throws Exception {
        when(syncExecutionQueryService.getSyncExecution(15L)).thenReturn(new SyncExecutionResponse(
                15L,
                9L,
                SyncExecutionStatus.SUCCESS,
                OffsetDateTime.parse("2026-05-15T15:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T15:01:00+09:00"),
                "MANUAL",
                "Saved 5 activities",
                null,
                OffsetDateTime.parse("2026-05-15T15:00:00+09:00")
        ));

        mockMvc.perform(get("/api/sync/executions/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.syncJobId").value(9));
    }
}
