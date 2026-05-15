package com.devactivityhub.projectsource.repository;

import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectSourceRepository extends JpaRepository<ProjectSource, Long> {

    boolean existsBySourceTypeAndExternalSourceId(ProjectSourceType sourceType, String externalSourceId);

    List<ProjectSource> findByProjectIdOrderByIdAsc(Long projectId);

    List<ProjectSource> findByProjectIdAndSourceType(Long projectId, ProjectSourceType sourceType);

    Optional<ProjectSource> findBySourceTypeAndExternalName(ProjectSourceType sourceType, String externalName);
}
