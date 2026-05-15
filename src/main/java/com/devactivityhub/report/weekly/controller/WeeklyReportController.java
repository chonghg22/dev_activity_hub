package com.devactivityhub.report.weekly.controller;

import com.devactivityhub.report.weekly.dto.WeeklyReportResponse;
import com.devactivityhub.report.weekly.service.WeeklyReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/weekly")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    public WeeklyReportController(WeeklyReportService weeklyReportService) {
        this.weeklyReportService = weeklyReportService;
    }

    @GetMapping
    public ResponseEntity<WeeklyReportResponse> getWeeklyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        return ResponseEntity.ok(weeklyReportService.getWeeklyReport(weekStartDate));
    }

    @PostMapping("/rebuild")
    public ResponseEntity<WeeklyReportResponse> rebuildWeeklyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        return ResponseEntity.ok(weeklyReportService.rebuildWeeklyReport(weekStartDate));
    }
}
