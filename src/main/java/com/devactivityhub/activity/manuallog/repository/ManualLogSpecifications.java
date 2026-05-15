package com.devactivityhub.activity.manuallog.repository;

import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ManualLogSpecifications {

    private ManualLogSpecifications() {
    }

    public static Specification<ManualLog> withFilters(
            Long projectId,
            ManualLogActivityType activityType,
            String tag,
            LocalDate from,
            LocalDate to,
            String keyword
    ) {
        return Specification.where(projectIdEquals(projectId))
                .and(activityTypeEquals(activityType))
                .and(tagEquals(tag))
                .and(workDateFrom(from))
                .and(workDateTo(to))
                .and(keywordContains(keyword));
    }

    private static Specification<ManualLog> projectIdEquals(Long projectId) {
        return (root, query, criteriaBuilder) ->
                projectId == null ? null : criteriaBuilder.equal(root.get("project").get("id"), projectId);
    }

    private static Specification<ManualLog> activityTypeEquals(ManualLogActivityType activityType) {
        return (root, query, criteriaBuilder) ->
                activityType == null ? null : criteriaBuilder.equal(root.get("activityType"), activityType);
    }

    private static Specification<ManualLog> tagEquals(String tag) {
        return (root, query, criteriaBuilder) -> {
            if (tag == null || tag.isBlank()) {
                return null;
            }

            query.distinct(true);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.join("tags", JoinType.LEFT).get("name")),
                    tag.trim().toLowerCase()
            );
        };
    }

    private static Specification<ManualLog> workDateFrom(LocalDate from) {
        return (root, query, criteriaBuilder) ->
                from == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("workDate"), from);
    }

    private static Specification<ManualLog> workDateTo(LocalDate to) {
        return (root, query, criteriaBuilder) ->
                to == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("workDate"), to);
    }

    private static Specification<ManualLog> keywordContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String normalized = "%" + keyword.trim().toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), normalized),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), normalized)
            );
        };
    }
}
