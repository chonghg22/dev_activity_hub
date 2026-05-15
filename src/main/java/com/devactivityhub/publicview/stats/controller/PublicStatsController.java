package com.devactivityhub.publicview.stats.controller;

import com.devactivityhub.publicview.stats.dto.PublicStatsSummaryResponse;
import com.devactivityhub.publicview.stats.dto.PublicWeeklyStatsResponse;
import com.devactivityhub.publicview.stats.service.PublicStatsQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/stats")
public class PublicStatsController {

    private static final CacheControl STATS_CACHE_CONTROL = CacheControl.maxAge(300, TimeUnit.SECONDS)
            .cachePublic()
            .mustRevalidate();

    private final PublicStatsQueryService publicStatsQueryService;

    public PublicStatsController(PublicStatsQueryService publicStatsQueryService) {
        this.publicStatsQueryService = publicStatsQueryService;
    }

    @GetMapping("/summary")
    public ResponseEntity<PublicStatsSummaryResponse> getSummary() {
        return ResponseEntity.ok()
                .cacheControl(STATS_CACHE_CONTROL)
                .body(publicStatsQueryService.getSummary());
    }

    @GetMapping("/weekly")
    public ResponseEntity<PublicWeeklyStatsResponse> getWeeklyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        return ResponseEntity.ok()
                .cacheControl(STATS_CACHE_CONTROL)
                .body(publicStatsQueryService.getWeeklyStats(weekStartDate));
    }
}
