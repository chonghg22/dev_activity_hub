package com.devactivityhub.activity.external.controller;

import com.devactivityhub.activity.external.dto.ExternalActivityResponse;
import com.devactivityhub.activity.external.service.ExternalActivityQueryService;
import com.devactivityhub.common.api.PageResponse;
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

@WebMvcTest(ExternalActivityController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class ExternalActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalActivityQueryService externalActivityQueryService;

    @Test
    void getExternalActivitiesSupportsFilters() throws Exception {
        when(externalActivityQueryService.getExternalActivities(
                1L,
                5L,
                "GITHUB",
                "COMMIT",
                java.time.LocalDate.of(2026, 5, 1),
                java.time.LocalDate.of(2026, 5, 31),
                "sync",
                0,
                20
        )).thenReturn(new PageResponse<>(
                List.of(sampleResponse()),
                0,
                20,
                1,
                1,
                true,
                true
        ));

        mockMvc.perform(get("/api/external-activities")
                        .param("projectId", "1")
                        .param("projectSourceId", "5")
                        .param("sourceType", "GITHUB")
                        .param("activityType", "COMMIT")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-31")
                        .param("keyword", "sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sourceId").value("commit:abc123"));
    }

    @Test
    void getExternalActivityReturnsDetail() throws Exception {
        when(externalActivityQueryService.getExternalActivity(7L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/external-activities/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectSlug").value("dev-activity-hub"))
                .andExpect(jsonPath("$.activityType").value("COMMIT"));
    }

    private ExternalActivityResponse sampleResponse() {
        return new ExternalActivityResponse(
                7L,
                1L,
                "dev-activity-hub",
                5L,
                "GITHUB",
                "commit:abc123",
                "COMMIT",
                "Implement sync service",
                "Implement sync service\n\nDetails",
                "chonghg22",
                "https://github.com/chonghg22/dev_activity_hub/commit/abc123",
                java.time.LocalDate.of(2026, 5, 15),
                OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                true,
                OffsetDateTime.parse("2026-05-15T10:05:00+09:00")
        );
    }
}
