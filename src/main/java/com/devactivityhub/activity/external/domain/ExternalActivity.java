package com.devactivityhub.activity.external.domain;

import com.devactivityhub.project.domain.Project;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "external_activities", schema = "dev_activity_hub")
public class ExternalActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_source_id")
    private ProjectSource projectSource;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "source_id", nullable = false, length = 120)
    private String sourceId;

    @Column(name = "activity_type", nullable = false, length = 40)
    private String activityType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content_summary")
    private String contentSummary;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Column(name = "activity_url", length = 300)
    private String activityUrl;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private JsonNode rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ExternalActivity() {
    }

    public ExternalActivity(Project project,
                            ProjectSource projectSource,
                            String sourceType,
                            String sourceId,
                            String activityType,
                            String title,
                            String contentSummary,
                            String actorName,
                            String activityUrl,
                            OffsetDateTime occurredAt,
                            boolean isPublic,
                            JsonNode rawPayload) {
        this.project = project;
        this.projectSource = projectSource;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.activityType = activityType;
        this.title = title;
        this.contentSummary = contentSummary;
        this.actorName = actorName;
        this.activityUrl = activityUrl;
        this.occurredAt = occurredAt;
        this.isPublic = isPublic;
        this.rawPayload = rawPayload;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public ProjectSource getProjectSource() {
        return projectSource;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getTitle() {
        return title;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActivityUrl() {
        return activityUrl;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
