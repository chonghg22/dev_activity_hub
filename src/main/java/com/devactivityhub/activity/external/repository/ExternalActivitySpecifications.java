package com.devactivityhub.activity.external.repository;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class ExternalActivitySpecifications {

    private ExternalActivitySpecifications() {
    }

    public static Specification<ExternalActivity> withFilters(
            Long projectId,
            Long projectSourceId,
            String sourceType,
            String activityType,
            LocalDate from,
            LocalDate to,
            String keyword
    ) {
        return Specification.where(projectIdEquals(projectId))
                .and(projectSourceIdEquals(projectSourceId))
                .and(sourceTypeEquals(sourceType))
                .and(activityTypeEquals(activityType))
                .and(occurredFrom(from))
                .and(occurredTo(to))
                .and(keywordContains(keyword));
    }

    private static Specification<ExternalActivity> projectIdEquals(Long projectId) {
        return (root, query, criteriaBuilder) ->
                projectId == null ? null : criteriaBuilder.equal(root.get("project").get("id"), projectId);
    }

    private static Specification<ExternalActivity> projectSourceIdEquals(Long projectSourceId) {
        return (root, query, criteriaBuilder) ->
                projectSourceId == null ? null : criteriaBuilder.equal(root.get("projectSource").get("id"), projectSourceId);
    }

    private static Specification<ExternalActivity> sourceTypeEquals(String sourceType) {
        return (root, query, criteriaBuilder) ->
                sourceType == null || sourceType.isBlank() ? null : criteriaBuilder.equal(root.get("sourceType"), sourceType);
    }

    private static Specification<ExternalActivity> activityTypeEquals(String activityType) {
        return (root, query, criteriaBuilder) ->
                activityType == null || activityType.isBlank() ? null : criteriaBuilder.equal(root.get("activityType"), activityType);
    }

    private static Specification<ExternalActivity> occurredFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> {
            if (from == null) {
                return null;
            }
            OffsetDateTime fromDateTime = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            return criteriaBuilder.greaterThanOrEqualTo(root.get("occurredAt"), fromDateTime);
        };
    }

    private static Specification<ExternalActivity> occurredTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> {
            if (to == null) {
                return null;
            }
            OffsetDateTime toDateTime = to.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            return criteriaBuilder.lessThan(root.get("occurredAt"), toDateTime);
        };
    }

    private static Specification<ExternalActivity> keywordContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String normalizedKeyword = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), normalizedKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("contentSummary")), normalizedKeyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("actorName")), normalizedKeyword)
            );
        };
    }
}
