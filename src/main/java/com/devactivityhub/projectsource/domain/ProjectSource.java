package com.devactivityhub.projectsource.domain;

import com.devactivityhub.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "project_sources", schema = "dev_activity_hub")
public class ProjectSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ProjectSourceType sourceType;

    @Column(name = "external_source_id", nullable = false, length = 120)
    private String externalSourceId;

    @Column(name = "external_name", nullable = false, length = 200)
    private String externalName;

    @Column(name = "external_url", length = 300)
    private String externalUrl;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ProjectSource() {
    }

    public ProjectSource(Project project,
                         ProjectSourceType sourceType,
                         String externalSourceId,
                         String externalName,
                         String externalUrl,
                         boolean isPrimary) {
        this.project = project;
        this.sourceType = sourceType;
        this.externalSourceId = externalSourceId;
        this.externalName = externalName;
        this.externalUrl = externalUrl;
        this.isPrimary = isPrimary;
    }

    public void update(Project project,
                       ProjectSourceType sourceType,
                       String externalSourceId,
                       String externalName,
                       String externalUrl,
                       Boolean isPrimary) {
        if (project != null) {
            this.project = project;
        }
        if (sourceType != null) {
            this.sourceType = sourceType;
        }
        if (externalSourceId != null) {
            this.externalSourceId = externalSourceId;
        }
        if (externalName != null) {
            this.externalName = externalName;
        }
        if (externalUrl != null || this.externalUrl != null) {
            this.externalUrl = externalUrl;
        }
        if (isPrimary != null) {
            this.isPrimary = isPrimary;
        }
    }

    public void updatePrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public ProjectSourceType getSourceType() {
        return sourceType;
    }

    public String getExternalSourceId() {
        return externalSourceId;
    }

    public String getExternalName() {
        return externalName;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
