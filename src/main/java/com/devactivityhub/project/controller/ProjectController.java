package com.devactivityhub.project.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.project.dto.ProjectCreateRequest;
import com.devactivityhub.project.dto.ProjectResponse;
import com.devactivityhub.project.dto.ProjectUpdateRequest;
import com.devactivityhub.project.dto.ProjectVisibilityUpdateRequest;
import com.devactivityhub.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public PageResponse<ProjectResponse> getProjects(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return projectService.getProjects(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.createProject(request);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable Long projectId) {
        return projectService.getProject(projectId);
    }

    @PatchMapping("/{projectId}")
    public ProjectResponse updateProject(@PathVariable Long projectId,
                                         @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.updateProject(projectId, request);
    }

    @PatchMapping("/{projectId}/visibility")
    public ProjectResponse updateVisibility(@PathVariable Long projectId,
                                            @Valid @RequestBody ProjectVisibilityUpdateRequest request) {
        return projectService.updateVisibility(projectId, request.isPublic());
    }
}
