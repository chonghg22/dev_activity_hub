package com.devactivityhub.publicview.stats.controller;

import com.devactivityhub.publicview.stats.dto.PublicActivityTypeMetricResponse;
import com.devactivityhub.publicview.stats.dto.PublicProjectMetricResponse;
import com.devactivityhub.publicview.stats.dto.PublicStatsSummaryResponse;
import com.devactivityhub.publicview.stats.dto.PublicWeeklyStatsResponse;
import com.devactivityhub.publicview.stats.service.PublicStatsQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicStatsController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class PublicStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicStatsQueryService publicStatsQueryService;

    @Test
    void getSummaryReturnsStats() throws Exception {
        when(publicStatsQueryService.getSummary()).thenReturn(
                new PublicStatsSummaryResponse(
                        2,
                        15,
                        4,
                        6,
                        2,
                        9,
                        List.of(new PublicActivityTypeMetricResponse("WORK_LOG", 10))
                )
        );

        mockMvc.perform(get("/api/public/stats/summary"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300, must-revalidate, public"))
                .andExpect(jsonPath("$.publicProjectCount").value(2))
                .andExpect(jsonPath("$.totalCommitCount").value(15))
                .andExpect(jsonPath("$.weeklyCommitCount").value(4))
                .andExpect(jsonPath("$.totalPullRequestActivityCount").value(6))
                .andExpect(jsonPath("$.weeklyPullRequestActivityCount").value(2))
                .andExpect(jsonPath("$.recent7DayActivityCount").value(9))
                .andExpect(jsonPath("$.activityTypeCounts[0].activityType").value("WORK_LOG"));
    }

    @Test
    void getWeeklyStatsReturnsWeekRange() throws Exception {
        when(publicStatsQueryService.getWeeklyStats(eq(LocalDate.of(2026, 5, 11)))).thenReturn(
                new PublicWeeklyStatsResponse(
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 17),
                        8,
                        2,
                        List.of(new PublicActivityTypeMetricResponse("LEARNING", 3)),
                        List.of(new PublicProjectMetricResponse("dev-activity-hub", "Dev Activity Hub", 5))
                )
        );

        mockMvc.perform(get("/api/public/stats/weekly")
                        .param("weekStartDate", "2026-05-11"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300, must-revalidate, public"))
                .andExpect(jsonPath("$.weekStartDate").value("2026-05-11"))
                .andExpect(jsonPath("$.projectCounts[0].projectSlug").value("dev-activity-hub"));
    }
}
