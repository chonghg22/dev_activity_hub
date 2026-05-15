package com.devactivityhub.activity.manuallog.repository;

import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import com.devactivityhub.publicview.stats.repository.PublicManualLogCountProjection;
import com.devactivityhub.publicview.stats.repository.PublicProjectCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ManualLogRepository extends JpaRepository<ManualLog, Long>, JpaSpecificationExecutor<ManualLog> {

    Page<ManualLog> findByVisibilityAndProjectIsPublicTrueAndProjectStatus(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus,
            Pageable pageable
    );

    long countByVisibilityAndProjectIsPublicTrueAndProjectStatus(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus
    );

    long countByVisibilityAndIsHighlightedTrueAndProjectIsPublicTrueAndProjectStatus(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus
    );

    @Query("""
            select ml.activityType as activityType, count(ml) as count
            from ManualLog ml
            where ml.visibility = :visibility
              and ml.project.isPublic = true
              and ml.project.status = :projectStatus
            group by ml.activityType
            order by count(ml) desc
            """)
    List<PublicManualLogCountProjection> countPublicLogsByActivityType(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus
    );

    long countByVisibilityAndProjectIsPublicTrueAndProjectStatusAndWorkDateBetween(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus,
            LocalDate from,
            LocalDate to
    );

    long countByVisibilityAndIsHighlightedTrueAndProjectIsPublicTrueAndProjectStatusAndWorkDateBetween(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus,
            LocalDate from,
            LocalDate to
    );

    @Query("""
            select ml.activityType as activityType, count(ml) as count
            from ManualLog ml
            where ml.visibility = :visibility
              and ml.project.isPublic = true
              and ml.project.status = :projectStatus
              and ml.workDate between :from and :to
            group by ml.activityType
            order by count(ml) desc
            """)
    List<PublicManualLogCountProjection> countPublicLogsByActivityTypeBetween(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus,
            LocalDate from,
            LocalDate to
    );

    @Query("""
            select ml.project.slug as projectSlug, ml.project.name as projectName, count(ml) as count
            from ManualLog ml
            where ml.visibility = :visibility
              and ml.project.isPublic = true
              and ml.project.status = :projectStatus
              and ml.workDate between :from and :to
            group by ml.project.slug, ml.project.name
            order by count(ml) desc, ml.project.name asc
            """)
    List<PublicProjectCountProjection> countPublicLogsByProjectBetween(
            ManualLogVisibility visibility,
            com.devactivityhub.project.domain.ProjectStatus projectStatus,
            LocalDate from,
            LocalDate to
    );
}
