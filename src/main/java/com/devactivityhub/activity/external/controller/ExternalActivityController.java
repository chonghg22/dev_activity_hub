package com.devactivityhub.activity.external.controller;

import com.devactivityhub.activity.external.dto.ExternalActivityResponse;
import com.devactivityhub.activity.external.service.ExternalActivityQueryService;
import com.devactivityhub.common.api.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/external-activities")
public class ExternalActivityController {

    private final ExternalActivityQueryService externalActivityQueryService;

    public ExternalActivityController(ExternalActivityQueryService externalActivityQueryService) {
        this.externalActivityQueryService = externalActivityQueryService;
    }

    @GetMapping
    public PageResponse<ExternalActivityResponse> getExternalActivities(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long projectSourceId,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return externalActivityQueryService.getExternalActivities(
                projectId,
                projectSourceId,
                sourceType,
                activityType,
                from,
                to,
                keyword,
                page,
                size
        );
    }

    @GetMapping("/{activityId}")
    public ExternalActivityResponse getExternalActivity(@PathVariable Long activityId) {
        return externalActivityQueryService.getExternalActivity(activityId);
    }
}
