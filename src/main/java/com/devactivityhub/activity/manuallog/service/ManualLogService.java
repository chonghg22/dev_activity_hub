package com.devactivityhub.activity.manuallog.service;

import com.devactivityhub.activity.manuallog.domain.ManualLog;
import com.devactivityhub.activity.manuallog.domain.ManualLogActivityType;
import com.devactivityhub.activity.manuallog.domain.ManualLogVisibility;
import com.devactivityhub.activity.manuallog.dto.ManualLogCreateRequest;
import com.devactivityhub.activity.manuallog.dto.ManualLogResponse;
import com.devactivityhub.activity.manuallog.dto.ManualLogUpdateRequest;
import com.devactivityhub.activity.manuallog.repository.ManualLogRepository;
import com.devactivityhub.activity.manuallog.repository.ManualLogSpecifications;
import com.devactivityhub.activity.tag.domain.Tag;
import com.devactivityhub.activity.tag.repository.TagRepository;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.service.ProjectService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManualLogService {

    private final ManualLogRepository manualLogRepository;
    private final TagRepository tagRepository;
    private final ProjectService projectService;

    public ManualLogService(ManualLogRepository manualLogRepository,
                            TagRepository tagRepository,
                            ProjectService projectService) {
        this.manualLogRepository = manualLogRepository;
        this.tagRepository = tagRepository;
        this.projectService = projectService;
    }

    @Transactional(readOnly = true)
    public List<ManualLogResponse> getManualLogs(Long projectId,
                                                 ManualLogActivityType activityType,
                                                 String tag,
                                                 LocalDate from,
                                                 LocalDate to,
                                                 String keyword) {
        return manualLogRepository.findAll(
                        ManualLogSpecifications.withFilters(projectId, activityType, tag, from, to, keyword),
                        Sort.by(Sort.Direction.DESC, "workDate").and(Sort.by(Sort.Direction.DESC, "id"))
                )
                .stream()
                .map(ManualLogResponse::from)
                .toList();
    }

    public ManualLogResponse createManualLog(ManualLogCreateRequest request) {
        validateTimeRange(request.startedAt(), request.endedAt());

        Project project = projectService.getProjectEntity(request.projectId());
        ManualLog manualLog = new ManualLog(
                project,
                request.title(),
                request.content(),
                request.activityType(),
                request.workDate(),
                request.startedAt(),
                request.endedAt(),
                request.visibility() == null ? ManualLogVisibility.PRIVATE : request.visibility(),
                Boolean.TRUE.equals(request.isHighlighted())
        );
        manualLog.replaceTags(resolveTags(request.tags()));

        return ManualLogResponse.from(manualLogRepository.save(manualLog));
    }

    @Transactional(readOnly = true)
    public ManualLogResponse getManualLog(Long logId) {
        return ManualLogResponse.from(getManualLogEntity(logId));
    }

    public ManualLogResponse updateManualLog(Long logId, ManualLogUpdateRequest request) {
        ManualLog manualLog = getManualLogEntity(logId);
        validateTimeRange(request.startedAt(), request.endedAt());

        Project project = request.projectId() == null ? null : projectService.getProjectEntity(request.projectId());
        manualLog.update(
                project,
                request.title(),
                request.content(),
                request.activityType(),
                request.workDate(),
                request.startedAt(),
                request.endedAt(),
                request.visibility(),
                request.isHighlighted()
        );

        if (request.tags() != null) {
            manualLog.replaceTags(resolveTags(request.tags()));
        }

        validateTimeRange(manualLog.getStartedAt(), manualLog.getEndedAt());
        return ManualLogResponse.from(manualLog);
    }

    public void deleteManualLog(Long logId) {
        ManualLog manualLog = getManualLogEntity(logId);
        manualLogRepository.delete(manualLog);
    }

    @Transactional(readOnly = true)
    public ManualLog getManualLogEntity(Long logId) {
        return manualLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Manual log not found. id=" + logId));
    }

    private void validateTimeRange(OffsetDateTime startedAt, OffsetDateTime endedAt) {
        if (startedAt != null && endedAt != null && startedAt.isAfter(endedAt)) {
            throw new IllegalArgumentException("Manual log startedAt must be on or before endedAt");
        }
    }

    private Set<Tag> resolveTags(Set<String> requestedTags) {
        if (requestedTags == null || requestedTags.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<String> normalizedTags = requestedTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Tag> existingTags = tagRepository.findByNameIn(normalizedTags);
        Map<String, Tag> existingTagMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        Set<Tag> resolvedTags = new LinkedHashSet<>(existingTags);
        for (String tagName : normalizedTags) {
            if (!existingTagMap.containsKey(tagName)) {
                resolvedTags.add(tagRepository.save(new Tag(tagName)));
            }
        }
        return resolvedTags;
    }
}
