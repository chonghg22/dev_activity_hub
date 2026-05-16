package com.devactivityhub.activity.external.repository;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.publicview.stats.repository.PublicManualLogCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExternalActivityRepository extends JpaRepository<ExternalActivity, Long>, JpaSpecificationExecutor<ExternalActivity> {

    Optional<ExternalActivity> findBySourceTypeAndSourceId(String sourceType, String sourceId);

    long countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityType(
            ProjectStatus projectStatus,
            String activityType
    );

    long countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeIn(
            ProjectStatus projectStatus,
            Collection<String> activityTypes
    );

    long countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            ProjectStatus projectStatus,
            String activityType,
            OffsetDateTime from,
            OffsetDateTime to
    );

    long countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndActivityTypeInAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            ProjectStatus projectStatus,
            Collection<String> activityTypes,
            OffsetDateTime from,
            OffsetDateTime to
    );

    long countByIsPublicTrueAndProjectIsPublicTrueAndProjectStatusAndOccurredAtGreaterThanEqualAndOccurredAtLessThan(
            ProjectStatus projectStatus,
            OffsetDateTime from,
            OffsetDateTime to
    );

    @Query("""
            select ea.activityType as activityType, count(ea) as count
            from ExternalActivity ea
            where ea.isPublic = true
              and ea.project.isPublic = true
              and ea.project.status = :projectStatus
            group by ea.activityType
            order by count(ea) desc
            """)
    List<PublicManualLogCountProjection> countPublicActivitiesByActivityType(ProjectStatus projectStatus);
}
