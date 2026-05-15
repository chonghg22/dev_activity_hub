package com.devactivityhub.publicview.project.service;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.repository.ProjectRepository;
import com.devactivityhub.publicview.project.dto.PublicProjectDetailResponse;
import com.devactivityhub.publicview.project.dto.PublicProjectListItemResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PublicProjectQueryService {

    private final ProjectRepository projectRepository;

    public PublicProjectQueryService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public PageResponse<PublicProjectListItemResponse> getPublicProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return PageResponse.from(
                projectRepository.findByIsPublicTrueAndStatus(ProjectStatus.ACTIVE, pageable)
                        .map(PublicProjectListItemResponse::from)
        );
    }

    public PublicProjectDetailResponse getPublicProject(String projectSlug) {
        return projectRepository.findBySlugAndIsPublicTrueAndStatus(projectSlug, ProjectStatus.ACTIVE)
                .map(PublicProjectDetailResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Public project not found. slug=" + projectSlug));
    }
}
