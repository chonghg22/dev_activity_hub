package com.devactivityhub.publicview.timeline.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.timeline.dto.PublicTimelineItemResponse;
import com.devactivityhub.publicview.timeline.service.PublicTimelineQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicTimelineController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class PublicTimelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicTimelineQueryService publicTimelineQueryService;

    @Test
    void getPublicTimelineReturnsPagedResponse() throws Exception {
        when(publicTimelineQueryService.getPublicTimeline(
                eq("dev-activity-hub"),
                eq("WORK_LOG"),
                eq(LocalDate.of(2026, 5, 1)),
                eq(LocalDate.of(2026, 5, 31)),
                eq(0),
                eq(20)
        )).thenReturn(new PageResponse<>(
                List.of(new PublicTimelineItemResponse(
                        1L,
                        "MANUAL_LOG",
                        OffsetDateTime.parse("2026-05-15T11:00:00+09:00"),
                        "dev-activity-hub",
                        "Dev Activity Hub",
                        "Added public timeline",
                        "Implemented public timeline endpoint.",
                        "WORK_LOG",
                        LocalDate.of(2026, 5, 15),
                        OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                        OffsetDateTime.parse("2026-05-15T11:00:00+09:00"),
                        true,
                        Set.of("public", "timeline")
                )),
                0,
                20,
                1,
                1,
                true,
                true
        ));

        mockMvc.perform(get("/api/public/timeline")
                        .param("projectSlug", "dev-activity-hub")
                        .param("activityType", "WORK_LOG")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=60, must-revalidate, public"))
                .andExpect(jsonPath("$.content[0].projectSlug").value("dev-activity-hub"));
    }
}
