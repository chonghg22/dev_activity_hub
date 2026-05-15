package com.devactivityhub.publicview.project.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.project.dto.PublicProjectDetailResponse;
import com.devactivityhub.publicview.project.dto.PublicProjectListItemResponse;
import com.devactivityhub.publicview.project.service.PublicProjectQueryService;
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

@WebMvcTest(PublicProjectController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class PublicProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicProjectQueryService publicProjectQueryService;

    @Test
    void getPublicProjectsReturnsPagedResponse() throws Exception {
        when(publicProjectQueryService.getPublicProjects(eq(0), eq(20)))
                .thenReturn(new PageResponse<>(
                        List.of(new PublicProjectListItemResponse("Dev Activity Hub", "dev-activity-hub", "portfolio backend", "PORTFOLIO")),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300, must-revalidate, public"))
                .andExpect(jsonPath("$.content[0].slug").value("dev-activity-hub"));
    }

    @Test
    void getPublicProjectReturnsDetail() throws Exception {
        when(publicProjectQueryService.getPublicProject("dev-activity-hub"))
                .thenReturn(new PublicProjectDetailResponse(
                        "Dev Activity Hub",
                        "dev-activity-hub",
                        "portfolio backend",
                        "PORTFOLIO",
                        LocalDate.of(2026, 5, 15),
                        null
                ));

        mockMvc.perform(get("/api/public/projects/dev-activity-hub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dev Activity Hub"));
    }
}
