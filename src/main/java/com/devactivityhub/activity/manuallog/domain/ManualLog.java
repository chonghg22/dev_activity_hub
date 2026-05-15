package com.devactivityhub.activity.manuallog.domain;

import com.devactivityhub.activity.tag.domain.Tag;
import com.devactivityhub.common.jpa.BaseTimeEntity;
import com.devactivityhub.project.domain.Project;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "manual_logs", schema = "dev_activity_hub")
public class ManualLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 40)
    private ManualLogActivityType activityType;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ManualLogVisibility visibility;

    @Column(name = "is_highlighted", nullable = false)
    private boolean isHighlighted;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "manual_log_tags",
            schema = "dev_activity_hub",
            joinColumns = @JoinColumn(name = "manual_log_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    protected ManualLog() {
    }

    public ManualLog(
            Project project,
            String title,
            String content,
            ManualLogActivityType activityType,
            LocalDate workDate,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            ManualLogVisibility visibility,
            boolean isHighlighted
    ) {
        this.project = project;
        this.title = title;
        this.content = content;
        this.activityType = activityType;
        this.workDate = workDate;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.visibility = visibility;
        this.isHighlighted = isHighlighted;
    }

    public void update(
            Project project,
            String title,
            String content,
            ManualLogActivityType activityType,
            LocalDate workDate,
            OffsetDateTime startedAt,
            OffsetDateTime endedAt,
            ManualLogVisibility visibility,
            Boolean isHighlighted
    ) {
        if (project != null) {
            this.project = project;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (activityType != null) {
            this.activityType = activityType;
        }
        if (workDate != null) {
            this.workDate = workDate;
        }
        if (startedAt != null) {
            this.startedAt = startedAt;
        }
        if (endedAt != null) {
            this.endedAt = endedAt;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (isHighlighted != null) {
            this.isHighlighted = isHighlighted;
        }
    }

    public void replaceTags(Set<Tag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public ManualLogActivityType getActivityType() {
        return activityType;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getEndedAt() {
        return endedAt;
    }

    public ManualLogVisibility getVisibility() {
        return visibility;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public Set<Tag> getTags() {
        return tags;
    }
}
