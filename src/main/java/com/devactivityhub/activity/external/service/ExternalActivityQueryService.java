package com.devactivityhub.activity.external.service;

import com.devactivityhub.activity.external.dto.ExternalActivityResponse;
import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
import com.devactivityhub.activity.external.repository.ExternalActivitySpecifications;
import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.common.error.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class ExternalActivityQueryService {

    private final ExternalActivityRepository externalActivityRepository;

    public ExternalActivityQueryService(ExternalActivityRepository externalActivityRepository) {
        this.externalActivityRepository = externalActivityRepository;
    }

    public PageResponse<ExternalActivityResponse> getExternalActivities(Long projectId,
                                                                        Long projectSourceId,
                                                                        String sourceType,
                                                                        String activityType,
                                                                        LocalDate from,
                                                                        LocalDate to,
                                                                        String keyword,
                                                                        int page,
                                                                        int size) {
        return PageResponse.from(
                externalActivityRepository.findAll(
                        ExternalActivitySpecifications.withFilters(projectId, projectSourceId, sourceType, activityType, from, to, keyword),
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))
                        )
                ).map(ExternalActivityResponse::from)
        );
    }

    public ExternalActivityResponse getExternalActivity(Long activityId) {
        return externalActivityRepository.findById(activityId)
                .map(ExternalActivityResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("External activity not found. id=" + activityId));
    }
}
