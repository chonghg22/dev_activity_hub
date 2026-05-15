package com.devactivityhub.projectsource.controller;

import com.devactivityhub.projectsource.dto.ProjectSourceCreateRequest;
import com.devactivityhub.projectsource.dto.ProjectSourceResponse;
import com.devactivityhub.projectsource.dto.ProjectSourceUpdateRequest;
import com.devactivityhub.projectsource.service.ProjectSourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/project-sources")
public class ProjectSourceController {

    private final ProjectSourceService projectSourceService;

    public ProjectSourceController(ProjectSourceService projectSourceService) {
        this.projectSourceService = projectSourceService;
    }

    @GetMapping
    public List<ProjectSourceResponse> getProjectSources(@RequestParam(required = false) Long projectId) {
        return projectSourceService.getProjectSources(projectId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectSourceResponse createProjectSource(@Valid @RequestBody ProjectSourceCreateRequest request) {
        return projectSourceService.createProjectSource(request);
    }

    @GetMapping("/{projectSourceId}")
    public ProjectSourceResponse getProjectSource(@PathVariable Long projectSourceId) {
        return projectSourceService.getProjectSource(projectSourceId);
    }

    @PatchMapping("/{projectSourceId}")
    public ProjectSourceResponse updateProjectSource(@PathVariable Long projectSourceId,
                                                     @Valid @RequestBody ProjectSourceUpdateRequest request) {
        return projectSourceService.updateProjectSource(projectSourceId, request);
    }

    @DeleteMapping("/{projectSourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProjectSource(@PathVariable Long projectSourceId) {
        projectSourceService.deleteProjectSource(projectSourceId);
    }
}
