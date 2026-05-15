package com.devactivityhub.publicview.project.controller;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.project.dto.PublicProjectDetailResponse;
import com.devactivityhub.publicview.project.dto.PublicProjectListItemResponse;
import com.devactivityhub.publicview.project.service.PublicProjectQueryService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/public/projects")
public class PublicProjectController {

    private static final CacheControl PROJECT_CACHE_CONTROL = CacheControl.maxAge(300, TimeUnit.SECONDS)
            .cachePublic()
            .mustRevalidate();

    private final PublicProjectQueryService publicProjectQueryService;

    public PublicProjectController(PublicProjectQueryService publicProjectQueryService) {
        this.publicProjectQueryService = publicProjectQueryService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PublicProjectListItemResponse>> getPublicProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .cacheControl(PROJECT_CACHE_CONTROL)
                .body(publicProjectQueryService.getPublicProjects(page, size));
    }

    @GetMapping("/{projectSlug}")
    public ResponseEntity<PublicProjectDetailResponse> getPublicProject(@PathVariable String projectSlug) {
        return ResponseEntity.ok()
                .cacheControl(PROJECT_CACHE_CONTROL)
                .body(publicProjectQueryService.getPublicProject(projectSlug));
    }
}
