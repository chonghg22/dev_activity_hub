package com.devactivityhub.report.weekly.domain;

import com.devactivityhub.project.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "weekly_report_items", schema = "dev_activity_hub")
public class WeeklyReportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    @Column(name = "source_kind", nullable = false, length = 20)
    private String sourceKind;

    @Column(name = "source_ref_id", nullable = false)
    private Long sourceRefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "activity_type", nullable = false, length = 40)
    private String activityType;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    protected WeeklyReportItem() {
    }

    public WeeklyReportItem(String sourceKind, Long sourceRefId, Project project,
                            String activityType, OffsetDateTime occurredAt, String title) {
        this.sourceKind = sourceKind;
        this.sourceRefId = sourceRefId;
        this.project = project;
        this.activityType = activityType;
        this.occurredAt = occurredAt;
        this.title = title;
    }

    void setWeeklyReport(WeeklyReport weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public Long getId() { return id; }
    public WeeklyReport getWeeklyReport() { return weeklyReport; }
    public String getSourceKind() { return sourceKind; }
    public Long getSourceRefId() { return sourceRefId; }
    public Project getProject() { return project; }
    public String getActivityType() { return activityType; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public String getTitle() { return title; }
}
