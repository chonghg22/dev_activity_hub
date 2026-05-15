package com.devactivityhub.project.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import com.devactivityhub.project.dto.ProjectResponse;
import com.devactivityhub.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    void getProjectsReturnsPage() throws Exception {
        var pageResponse = new PageResponse<>(List.of(sampleProjectResponse()), 0, 20, 1, 1, true, true);
        when(projectService.getProjects(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("dev-activity-hub"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void createProjectReturnsCreated() throws Exception {
        when(projectService.createProject(any())).thenReturn(sampleProjectResponse());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dev Activity Hub",
                                  "slug": "dev-activity-hub",
                                  "description": "portfolio backend",
                                  "category": "PORTFOLIO",
                                  "visibility": "PRIVATE",
                                  "isPublic": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dev Activity Hub"));
    }

    @Test
    void updateVisibilityDelegatesToService() throws Exception {
        when(projectService.updateVisibility(eq(1L), eq(true))).thenReturn(sampleProjectResponse());

        mockMvc.perform(patch("/api/projects/1/visibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "isPublic": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(projectService).updateVisibility(1L, true);
    }

    private ProjectResponse sampleProjectResponse() {
        return new ProjectResponse(
                1L,
                "Dev Activity Hub",
                "dev-activity-hub",
                "portfolio backend",
                "PORTFOLIO",
                ProjectVisibility.PRIVATE,
                false,
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 15),
                null,
                OffsetDateTime.parse("2026-05-15T09:00:00+09:00"),
                OffsetDateTime.parse("2026-05-15T09:00:00+09:00")
        );
    }
}
