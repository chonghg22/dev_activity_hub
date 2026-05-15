package com.devactivityhub.activity.manuallog.controller;

import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.dto.ManualLogCreateRequest;
import com.devactivityhub.activity.manuallog.dto.ManualLogResponse;
import com.devactivityhub.activity.manuallog.dto.ManualLogUpdateRequest;
import com.devactivityhub.activity.manuallog.service.ManualLogService;
import com.devactivityhub.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/manual-logs")
public class ManualLogController {

    private final ManualLogService manualLogService;

    public ManualLogController(ManualLogService manualLogService) {
        this.manualLogService = manualLogService;
    }

    @GetMapping
    public PageResponse<ManualLogResponse> getManualLogs(@RequestParam(required = false) Long projectId,
                                                         @RequestParam(required = false) ManualLogActivityType activityType,
                                                         @RequestParam(required = false) String tag,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return manualLogService.getManualLogs(projectId, activityType, tag, from, to, keyword, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManualLogResponse createManualLog(@Valid @RequestBody ManualLogCreateRequest request) {
        return manualLogService.createManualLog(request);
    }

    @GetMapping("/{logId}")
    public ManualLogResponse getManualLog(@PathVariable Long logId) {
        return manualLogService.getManualLog(logId);
    }

    @PatchMapping("/{logId}")
    public ManualLogResponse updateManualLog(@PathVariable Long logId,
                                             @Valid @RequestBody ManualLogUpdateRequest request) {
        return manualLogService.updateManualLog(logId, request);
    }

    @DeleteMapping("/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteManualLog(@PathVariable Long logId) {
        manualLogService.deleteManualLog(logId);
    }
}
