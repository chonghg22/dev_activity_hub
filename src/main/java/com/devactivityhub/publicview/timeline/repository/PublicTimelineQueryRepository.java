package com.devactivityhub.publicview.timeline.repository;

import com.devactivityhub.publicview.timeline.dto.PublicTimelineItemResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PublicTimelineQueryRepository {

    private final EntityManager entityManager;

    public PublicTimelineQueryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<PublicTimelineItemResponse> findPublicTimeline(
            String projectSlug,
            String activityType,
            LocalDate from,
            LocalDate to,
            int offset,
            int limit
    ) {
        StringBuilder sql = buildUnionSql(projectSlug, activityType, from, to);
        sql.append(" ORDER BY occurred_at DESC NULLS LAST, id DESC");
        sql.append(" LIMIT :limit OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString());
        bindParameters(query, projectSlug, activityType, from, to);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream().map(this::mapRow).toList();
    }

    public long countPublicTimeline(
            String projectSlug,
            String activityType,
            LocalDate from,
            LocalDate to
    ) {
        StringBuilder inner = buildUnionSql(projectSlug, activityType, from, to);
        String sql = "SELECT COUNT(*) FROM (" + inner + ") cnt";

        Query query = entityManager.createNativeQuery(sql);
        bindParameters(query, projectSlug, activityType, from, to);

        return ((Number) query.getSingleResult()).longValue();
    }

    private StringBuilder buildUnionSql(String projectSlug, String activityType, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");

        // Manual logs
        sql.append("""
                SELECT ml.id,
                       'MANUAL_LOG' AS source_kind,
                       COALESCE(ml.ended_at, ml.started_at, ml.created_at) AS occurred_at,
                       p.slug AS project_slug,
                       p.name AS project_name,
                       ml.title,
                       ml.content,
                       ml.activity_type,
                       ml.work_date,
                       ml.started_at,
                       ml.ended_at,
                       ml.is_highlighted AS highlighted,
                       COALESCE((SELECT string_agg(t.name, ',' ORDER BY t.name)
                                 FROM dev_activity_hub.manual_log_tags mlt
                                 JOIN dev_activity_hub.tags t ON t.id = mlt.tag_id
                                 WHERE mlt.manual_log_id = ml.id), '') AS tags_csv
                FROM dev_activity_hub.manual_logs ml
                JOIN dev_activity_hub.projects p ON p.id = ml.project_id
                WHERE ml.visibility = 'PUBLIC'
                  AND p.is_public = true
                  AND p.status = 'ACTIVE'
                """);
        if (projectSlug != null) {
            sql.append(" AND p.slug = :projectSlug");
        }
        if (activityType != null) {
            sql.append(" AND ml.activity_type = :activityType");
        }
        if (from != null) {
            sql.append(" AND ml.work_date >= :fromDate");
        }
        if (to != null) {
            sql.append(" AND ml.work_date <= :toDate");
        }

        sql.append(" UNION ALL ");

        // External activities
        sql.append("""
                SELECT ea.id,
                       'EXTERNAL_ACTIVITY' AS source_kind,
                       ea.occurred_at,
                       p.slug AS project_slug,
                       p.name AS project_name,
                       ea.title,
                       ea.content_summary AS content,
                       ea.activity_type,
                       CAST(ea.occurred_at AS date) AS work_date,
                       ea.occurred_at AS started_at,
                       ea.occurred_at AS ended_at,
                       false AS highlighted,
                       '' AS tags_csv
                FROM dev_activity_hub.external_activities ea
                JOIN dev_activity_hub.projects p ON p.id = ea.project_id
                WHERE ea.is_public = true
                  AND p.is_public = true
                  AND p.status = 'ACTIVE'
                """);
        if (projectSlug != null) {
            sql.append(" AND p.slug = :projectSlug");
        }
        if (activityType != null) {
            sql.append(" AND ea.activity_type = :activityType");
        }
        if (from != null) {
            sql.append(" AND ea.occurred_at >= :fromTimestamp");
        }
        if (to != null) {
            sql.append(" AND ea.occurred_at < :toTimestamp");
        }

        sql.append(") t");
        return sql;
    }

    private void bindParameters(Query query, String projectSlug, String activityType, LocalDate from, LocalDate to) {
        if (projectSlug != null) {
            query.setParameter("projectSlug", projectSlug);
        }
        if (activityType != null) {
            query.setParameter("activityType", activityType);
        }
        if (from != null) {
            query.setParameter("fromDate", from);
            query.setParameter("fromTimestamp", from.atStartOfDay().atOffset(ZoneOffset.UTC));
        }
        if (to != null) {
            query.setParameter("toDate", to);
            query.setParameter("toTimestamp", to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
        }
    }

    private PublicTimelineItemResponse mapRow(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String sourceKind = (String) row[1];
        OffsetDateTime occurredAt = toOffsetDateTime(row[2]);
        String projectSlug = (String) row[3];
        String projectName = (String) row[4];
        String title = (String) row[5];
        String content = (String) row[6];
        String activityType = (String) row[7];
        LocalDate workDate = toLocalDate(row[8]);
        OffsetDateTime startedAt = toOffsetDateTime(row[9]);
        OffsetDateTime endedAt = toOffsetDateTime(row[10]);
        boolean highlighted = toBoolean(row[11]);
        String tagsCsv = (String) row[12];

        Set<String> tags = (tagsCsv == null || tagsCsv.isEmpty())
                ? new LinkedHashSet<>()
                : Arrays.stream(tagsCsv.split(","))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return new PublicTimelineItemResponse(
                id, sourceKind, occurredAt, projectSlug, projectName,
                title, content, activityType, workDate, startedAt, endedAt,
                highlighted, tags
        );
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof OffsetDateTime odt) return odt;
        if (value instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }

    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        return false;
    }
}
