package com.devactivityhub.project.domain;

import com.devactivityhub.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "projects", schema = "dev_activity_hub")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, length = 80, unique = true)
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ProjectVisibility visibility;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "started_on")
    private LocalDate startedOn;

    @Column(name = "ended_on")
    private LocalDate endedOn;

    protected Project() {
    }

    public Project(
            String name,
            String slug,
            String description,
            String category,
            ProjectVisibility visibility,
            boolean isPublic,
            ProjectStatus status,
            LocalDate startedOn,
            LocalDate endedOn
    ) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.category = category;
        this.visibility = visibility;
        this.isPublic = isPublic;
        this.status = status;
        this.startedOn = startedOn;
        this.endedOn = endedOn;
    }

    public void update(
            String name,
            String slug,
            String description,
            String category,
            ProjectVisibility visibility,
            Boolean isPublic,
            ProjectStatus status,
            LocalDate startedOn,
            LocalDate endedOn
    ) {
        if (name != null) {
            this.name = name;
        }
        if (slug != null) {
            this.slug = slug;
        }
        if (description != null) {
            this.description = description;
        }
        if (category != null) {
            this.category = category;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (isPublic != null) {
            this.isPublic = isPublic;
        }
        if (status != null) {
            this.status = status;
        }
        if (startedOn != null || this.startedOn != null) {
            this.startedOn = startedOn;
        }
        if (endedOn != null || this.endedOn != null) {
            this.endedOn = endedOn;
        }
    }

    public void updatePublicFlag(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public ProjectVisibility getVisibility() {
        return visibility;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDate getStartedOn() {
        return startedOn;
    }

    public LocalDate getEndedOn() {
        return endedOn;
    }
}
