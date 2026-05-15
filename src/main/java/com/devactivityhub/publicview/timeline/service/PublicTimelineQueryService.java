package com.devactivityhub.publicview.timeline.service;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.timeline.dto.PublicTimelineItemResponse;
import com.devactivityhub.publicview.timeline.repository.PublicTimelineQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PublicTimelineQueryService {

    private final PublicTimelineQueryRepository timelineQueryRepository;

    public PublicTimelineQueryService(PublicTimelineQueryRepository timelineQueryRepository) {
        this.timelineQueryRepository = timelineQueryRepository;
    }

    public PageResponse<PublicTimelineItemResponse> getPublicTimeline(
            String projectSlug,
            String activityType,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        long totalElements = timelineQueryRepository.countPublicTimeline(projectSlug, activityType, from, to);
        List<PublicTimelineItemResponse> content = timelineQueryRepository.findPublicTimeline(
                projectSlug, activityType, from, to, page * size, size
        );
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page == 0,
                (long) (page + 1) * size >= totalElements
        );
    }
}
