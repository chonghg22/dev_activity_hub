package com.devactivityhub.project.repository;

import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsBySlug(String slug);

    Page<Project> findByIsPublicTrueAndStatus(ProjectStatus status, Pageable pageable);

    Optional<Project> findBySlugAndIsPublicTrueAndStatus(String slug, ProjectStatus status);

    long countByIsPublicTrueAndStatus(ProjectStatus status);
}
