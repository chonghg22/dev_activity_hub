package com.devactivityhub.report.weekly.controller;

import com.devactivityhub.report.weekly.dto.WeeklyReportResponse;
import com.devactivityhub.report.weekly.service.WeeklyReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeeklyReportController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class WeeklyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeeklyReportService weeklyReportService;

    @Test
    void getWeeklyReportReturnsReport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode summary = mapper.createObjectNode();
        summary.put("totalActivities", 5);

        when(weeklyReportService.getWeeklyReport(LocalDate.of(2026, 5, 11)))
                .thenReturn(new WeeklyReportResponse(
                        1L,
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 17),
                        OffsetDateTime.parse("2026-05-15T12:00:00+09:00"),
                        summary,
                        List.of()
                ));

        mockMvc.perform(get("/api/reports/weekly")
                        .param("weekStartDate", "2026-05-11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStartDate").value("2026-05-11"))
                .andExpect(jsonPath("$.summary.totalActivities").value(5));
    }

    @Test
    void rebuildWeeklyReportReturnsRebuiltReport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode summary = mapper.createObjectNode();
        summary.put("totalActivities", 3);

        when(weeklyReportService.rebuildWeeklyReport(LocalDate.of(2026, 5, 12)))
                .thenReturn(new WeeklyReportResponse(
                        1L,
                        LocalDate.of(2026, 5, 11),
                        LocalDate.of(2026, 5, 17),
                        OffsetDateTime.parse("2026-05-15T12:00:00+09:00"),
                        summary,
                        List.of()
                ));

        mockMvc.perform(post("/api/reports/weekly/rebuild")
                        .param("weekStartDate", "2026-05-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStartDate").value("2026-05-11"))
                .andExpect(jsonPath("$.summary.totalActivities").value(3));
    }
}
