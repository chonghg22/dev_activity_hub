package com.devactivityhub.projectsource.service;

import com.devactivityhub.common.error.DuplicateResourceException;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.service.ProjectService;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.dto.ProjectSourceCreateRequest;
import com.devactivityhub.projectsource.dto.ProjectSourceResponse;
import com.devactivityhub.projectsource.dto.ProjectSourceUpdateRequest;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.job.service.SyncJobService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class ProjectSourceService {

    private final ProjectSourceRepository projectSourceRepository;
    private final ProjectService projectService;
    private final SyncJobService syncJobService;

    public ProjectSourceService(ProjectSourceRepository projectSourceRepository,
                                ProjectService projectService,
                                SyncJobService syncJobService) {
        this.projectSourceRepository = projectSourceRepository;
        this.projectService = projectService;
        this.syncJobService = syncJobService;
    }

    @Transactional(readOnly = true)
    public List<ProjectSourceResponse> getProjectSources(Long projectId) {
        List<ProjectSource> sources = projectId == null
                ? projectSourceRepository.findAll()
                : projectSourceRepository.findByProjectIdOrderByIdAsc(projectId);
        return sources.stream()
                .map(ProjectSourceResponse::from)
                .toList();
    }

    public ProjectSourceResponse createProjectSource(ProjectSourceCreateRequest request) {
        validateGithubSource(request.sourceType(), request.externalName(), request.externalUrl());
        ensureExternalSourceUnique(request.sourceType(), request.externalSourceId(), null);

        Project project = projectService.getProjectEntity(request.projectId());
        ProjectSource projectSource = new ProjectSource(
                project,
                request.sourceType(),
                request.externalSourceId(),
                request.externalName(),
                request.externalUrl(),
                Boolean.TRUE.equals(request.isPrimary())
        );

        ProjectSource saved = projectSourceRepository.save(projectSource);
        normalizePrimarySource(saved);
        syncJobService.ensureSyncJob(saved);
        return ProjectSourceResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ProjectSourceResponse getProjectSource(Long projectSourceId) {
        return ProjectSourceResponse.from(getProjectSourceEntity(projectSourceId));
    }

    public ProjectSourceResponse updateProjectSource(Long projectSourceId, ProjectSourceUpdateRequest request) {
        ProjectSource projectSource = getProjectSourceEntity(projectSourceId);
        validateGithubSource(
                request.sourceType() == null ? projectSource.getSourceType() : request.sourceType(),
                request.externalName() == null ? projectSource.getExternalName() : request.externalName(),
                request.externalUrl() == null ? projectSource.getExternalUrl() : request.externalUrl()
        );

        ensureExternalSourceUnique(
                request.sourceType() == null ? projectSource.getSourceType() : request.sourceType(),
                request.externalSourceId() == null ? projectSource.getExternalSourceId() : request.externalSourceId(),
                projectSourceId
        );

        String previousJobName = buildJobName(projectSource.getSourceType(), projectSource.getExternalName());

        Project project = request.projectId() == null ? null : projectService.getProjectEntity(request.projectId());
        projectSource.update(
                project,
                request.sourceType(),
                request.externalSourceId(),
                request.externalName(),
                request.externalUrl(),
                request.isPrimary()
        );

        if (Boolean.TRUE.equals(request.isPrimary())) {
            normalizePrimarySource(projectSource);
        }

        syncJobService.syncJobName(projectSource, previousJobName);
        return ProjectSourceResponse.from(projectSource);
    }

    public void deleteProjectSource(Long projectSourceId) {
        ProjectSource projectSource = getProjectSourceEntity(projectSourceId);
        syncJobService.deleteSyncJob(projectSource);
        projectSourceRepository.delete(projectSource);
    }

    @Transactional(readOnly = true)
    public ProjectSource getProjectSourceEntity(Long projectSourceId) {
        return projectSourceRepository.findById(projectSourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Project source not found. id=" + projectSourceId));
    }

    private void ensureExternalSourceUnique(ProjectSourceType sourceType, String externalSourceId, Long currentProjectSourceId) {
        boolean exists = projectSourceRepository.existsBySourceTypeAndExternalSourceId(sourceType, externalSourceId);
        if (!exists) {
            return;
        }

        if (currentProjectSourceId != null) {
            ProjectSource current = getProjectSourceEntity(currentProjectSourceId);
            if (current.getSourceType() == sourceType && current.getExternalSourceId().equals(externalSourceId)) {
                return;
            }
        }

        throw new DuplicateResourceException("Project source already exists. sourceType=" + sourceType + ", externalSourceId=" + externalSourceId);
    }

    private void normalizePrimarySource(ProjectSource projectSource) {
        if (!projectSource.isPrimary()) {
            return;
        }

        List<ProjectSource> siblingSources = projectSourceRepository.findByProjectIdAndSourceType(
                projectSource.getProject().getId(),
                projectSource.getSourceType()
        );

        siblingSources.stream()
                .filter(source -> source != projectSource && !Objects.equals(source.getId(), projectSource.getId()))
                .forEach(source -> source.updatePrimary(false));
    }

    private void validateGithubSource(ProjectSourceType sourceType, String externalName, String externalUrl) {
        if (sourceType != ProjectSourceType.GITHUB) {
            return;
        }

        if (externalName == null || !externalName.contains("/")) {
            throw new IllegalArgumentException("GitHub source externalName must be in owner/repository format");
        }

        if (externalUrl != null && !externalUrl.isBlank() && !externalUrl.startsWith("https://github.com/")) {
            throw new IllegalArgumentException("GitHub source externalUrl must start with https://github.com/");
        }
    }

    private String buildJobName(ProjectSourceType sourceType, String externalName) {
        return switch (sourceType) {
            case GITHUB -> "github:" + externalName.trim().toLowerCase(Locale.ROOT);
        };
    }
}
