package com.devactivityhub.project.service;

import com.devactivityhub.common.error.DuplicateResourceException;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.dto.ProjectCreateRequest;
import com.devactivityhub.project.dto.ProjectResponse;
import com.devactivityhub.project.dto.ProjectUpdateRequest;
import com.devactivityhub.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        validateDateRange(request.startedOn(), request.endedOn());
        ensureSlugAvailable(request.slug(), null);

        Project project = new Project(
                request.name(),
                request.slug(),
                request.description(),
                request.category(),
                request.visibility(),
                Boolean.TRUE.equals(request.isPublic()),
                request.status() == null ? ProjectStatus.ACTIVE : request.status(),
                request.startedOn(),
                request.endedOn()
        );

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        return ProjectResponse.from(getProjectEntity(projectId));
    }

    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        Project project = getProjectEntity(projectId);
        validateDateRange(request.startedOn(), request.endedOn());
        ensureSlugAvailable(request.slug(), projectId);

        project.update(
                request.name(),
                request.slug(),
                request.description(),
                request.category(),
                request.visibility(),
                request.isPublic(),
                request.status(),
                request.startedOn(),
                request.endedOn()
        );

        validateDateRange(project.getStartedOn(), project.getEndedOn());
        return ProjectResponse.from(project);
    }

    public ProjectResponse updateVisibility(Long projectId, boolean isPublic) {
        Project project = getProjectEntity(projectId);
        project.updatePublicFlag(isPublic);
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found. id=" + projectId));
    }

    private void ensureSlugAvailable(String slug, Long currentProjectId) {
        if (slug == null) {
            return;
        }

        boolean exists = projectRepository.existsBySlug(slug);
        if (!exists) {
            return;
        }

        if (currentProjectId != null) {
            Project currentProject = getProjectEntity(currentProjectId);
            if (slug.equals(currentProject.getSlug())) {
                return;
            }
        }

        throw new DuplicateResourceException("Project slug already exists. slug=" + slug);
    }

    private void validateDateRange(java.time.LocalDate startedOn, java.time.LocalDate endedOn) {
        if (startedOn != null && endedOn != null && startedOn.isAfter(endedOn)) {
            throw new IllegalArgumentException("Project startedOn must be on or before endedOn");
        }
    }
}
