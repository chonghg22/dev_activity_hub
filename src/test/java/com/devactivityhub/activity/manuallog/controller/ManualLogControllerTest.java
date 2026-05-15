package com.devactivityhub.activity.manuallog.controller;

import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import com.devactivityhub.activity.manuallog.dto.ManualLogResponse;
import com.devactivityhub.activity.manuallog.dto.ProjectReferenceResponse;
import com.devactivityhub.activity.manuallog.service.ManualLogService;
import com.devactivityhub.common.api.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManualLogController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class ManualLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManualLogService manualLogService;

    @Test
    void getManualLogsSupportsFilters() throws Exception {
        var pageResponse = new PageResponse<>(List.of(sampleResponse()), 0, 20, 1, 1, true, true);
        when(manualLogService.getManualLogs(1L, ManualLogActivityType.WORK_LOG, "spring", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), "cache", 0, 20))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/manual-logs")
                        .param("projectId", "1")
                        .param("activityType", "WORK_LOG")
                        .param("tag", "spring")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-31")
                        .param("keyword", "cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tags[0]").value("spring"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void createManualLogReturnsCreated() throws Exception {
        when(manualLogService.createManualLog(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/manual-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "title": "Added public API draft",
                                  "content": "Implemented project and manual log endpoints.",
                                  "activityType": "WORK_LOG",
                                  "workDate": "2026-05-15",
                                  "visibility": "PRIVATE",
                                  "isHighlighted": true,
                                  "tags": ["spring", "api"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.project.slug").value("dev-activity-hub"));
    }

    @Test
    void deleteManualLogReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/manual-logs/3"))
                .andExpect(status().isNoContent());

        verify(manualLogService).deleteManualLog(3L);
    }

    private ManualLogResponse sampleResponse() {
        return new ManualLogResponse(
                3L,
                new ProjectReferenceResponse(1L, "Dev Activity Hub", "dev-activity-hub"),
                "Added public API draft",
                "Implemented project and manual log endpoints.",
                ManualLogActivityType.WORK_LOG,
                LocalDate.of(2026, 5, 15),
                OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T11:00:00+09:00"),
                ManualLogVisibility.PRIVATE,
                true,
                new LinkedHashSet<>(List.of("spring", "api")),
                OffsetDateTime.parse("2026-05-15T11:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T11:00:00+09:00")
        );
    }
}
