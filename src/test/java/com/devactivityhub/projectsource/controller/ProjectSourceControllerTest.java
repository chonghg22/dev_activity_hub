package com.devactivityhub.projectsource.controller;

import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.dto.ProjectSourceResponse;
import com.devactivityhub.projectsource.service.ProjectSourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectSourceController.class)
@Import(com.devactivityhub.common.error.GlobalExceptionHandler.class)
class ProjectSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectSourceService projectSourceService;

    @Test
    void getProjectSourcesSupportsProjectFilter() throws Exception {
        when(projectSourceService.getProjectSources(1L)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/project-sources")
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalName").value("chonghg22/dev_activity_hub"));
    }

    @Test
    void createProjectSourceReturnsCreated() throws Exception {
        when(projectSourceService.createProjectSource(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/project-sources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "projectId": 1,
                                  "sourceType": "GITHUB",
                                  "externalSourceId": "101",
                                  "externalName": "chonghg22/dev_activity_hub",
                                  "externalUrl": "https://github.com/chonghg22/dev_activity_hub",
                                  "isPrimary": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceType").value("GITHUB"))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    void deleteProjectSourceReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/project-sources/5"))
                .andExpect(status().isNoContent());

        verify(projectSourceService).deleteProjectSource(5L);
    }

    private ProjectSourceResponse sampleResponse() {
        return new ProjectSourceResponse(
                5L,
                1L,
                "dev-activity-hub",
                ProjectSourceType.GITHUB,
                "101",
                "chonghg22/dev_activity_hub",
                "https://github.com/chonghg22/dev_activity_hub",
                true,
                OffsetDateTime.parse("2026-05-15T14:00:00+09:00")
        );
    }
}
