package com.devactivityhub.publicview.timeline.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.timeline.dto.PublicTimelineItemResponse;
import com.devactivityhub.publicview.timeline.service.PublicTimelineQueryService;
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
@RequestMapping("/api/public/timeline")
public class PublicTimelineController {

    private static final CacheControl TIMELINE_CACHE_CONTROL = CacheControl.maxAge(60, TimeUnit.SECONDS)
            .cachePublic()
            .mustRevalidate();

    private final PublicTimelineQueryService publicTimelineQueryService;

    public PublicTimelineController(PublicTimelineQueryService publicTimelineQueryService) {
        this.publicTimelineQueryService = publicTimelineQueryService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PublicTimelineItemResponse>> getPublicTimeline(
            @RequestParam(required = false) String projectSlug,
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .cacheControl(TIMELINE_CACHE_CONTROL)
                .body(publicTimelineQueryService.getPublicTimeline(projectSlug, activityType, from, to, page, size));
    }
}
