package com.devactivityhub.report.weekly.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_reports", schema = "dev_activity_hub")
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start_date", nullable = false, unique = true)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary_json", nullable = false)
    private JsonNode summaryJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WeeklyReportItem> items = new ArrayList<>();

    protected WeeklyReport() {
    }

    public WeeklyReport(LocalDate weekStartDate, LocalDate weekEndDate, OffsetDateTime generatedAt, JsonNode summaryJson) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.generatedAt = generatedAt;
        this.summaryJson = summaryJson;
        this.createdAt = OffsetDateTime.now();
    }

    public void rebuild(OffsetDateTime generatedAt, JsonNode summaryJson, List<WeeklyReportItem> newItems) {
        this.generatedAt = generatedAt;
        this.summaryJson = summaryJson;
        this.items.clear();
        for (WeeklyReportItem item : newItems) {
            addItem(item);
        }
    }

    public void addItem(WeeklyReportItem item) {
        items.add(item);
        item.setWeeklyReport(this);
    }

    public Long getId() { return id; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public LocalDate getWeekEndDate() { return weekEndDate; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public JsonNode getSummaryJson() { return summaryJson; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<WeeklyReportItem> getItems() { return items; }
}
