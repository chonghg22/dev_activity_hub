package com.devactivityhub.publicview.timeline.service;

import com.devactivityhub.common.api.PageResponse;
import com.devactivityhub.publicview.timeline.dto.PublicTimelineItemResponse;
import com.devactivityhub.publicview.timeline.repository.PublicTimelineQueryRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicTimelineQueryServiceTest {

    private final PublicTimelineQueryRepository timelineQueryRepository = mock(PublicTimelineQueryRepository.class);
    private final PublicTimelineQueryService publicTimelineQueryService =
            new PublicTimelineQueryService(timelineQueryRepository);

    @Test
    void getPublicTimelineReturnsMergedResultsFromUnionQuery() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);

        when(timelineQueryRepository.countPublicTimeline("dev-activity-hub", null, from, to))
                .thenReturn(2L);
        when(timelineQueryRepository.findPublicTimeline("dev-activity-hub", null, from, to, 0, 20))
                .thenReturn(List.of(
                        new PublicTimelineItemResponse(
                                2L, "EXTERNAL_ACTIVITY",
                                OffsetDateTime.parse("2026-05-15T12:00:00+09:00"),
                                "dev-activity-hub", "Dev Activity Hub",
                                "External item", "external content", "COMMIT",
                                LocalDate.of(2026, 5, 15),
                                OffsetDateTime.parse("2026-05-15T12:00:00+09:00"),
                                OffsetDateTime.parse("2026-05-15T12:00:00+09:00"),
                                false, new LinkedHashSet<>()
                        ),
                        new PublicTimelineItemResponse(
                                1L, "MANUAL_LOG",
                                OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                                "dev-activity-hub", "Dev Activity Hub",
                                "Manual item", "manual content", "WORK_LOG",
                                LocalDate.of(2026, 5, 15),
                                OffsetDateTime.parse("2026-05-15T09:00:00+09:00"),
                                OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                                true, Set.of("spring")
                        )
                ));

        PageResponse<PublicTimelineItemResponse> response = publicTimelineQueryService.getPublicTimeline(
                "dev-activity-hub", null, from, to, 0, 20
        );

        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).sourceKind()).isEqualTo("EXTERNAL_ACTIVITY");
        assertThat(response.content().get(0).title()).isEqualTo("External item");
        assertThat(response.content().get(1).sourceKind()).isEqualTo("MANUAL_LOG");
        assertThat(response.content().get(1).tags()).containsExactly("spring");
    }

    @Test
    void getPublicTimelineCalculatesPagingCorrectly() {
        when(timelineQueryRepository.countPublicTimeline(null, null, null, null))
                .thenReturn(4L);
        when(timelineQueryRepository.findPublicTimeline(null, null, null, null, 2, 2))
                .thenReturn(List.of(
                        new PublicTimelineItemResponse(
                                1L, "MANUAL_LOG",
                                OffsetDateTime.parse("2026-05-15T10:00:00+09:00"),
                                "dev-activity-hub", "Dev Activity Hub",
                                "Manual item 1", "content", "WORK_LOG",
                                LocalDate.of(2026, 5, 15), null, null,
                                false, new LinkedHashSet<>()
                        ),
                        new PublicTimelineItemResponse(
                                2L, "MANUAL_LOG",
                                OffsetDateTime.parse("2026-05-15T09:00:00+09:00"),
                                "dev-activity-hub", "Dev Activity Hub",
                                "Manual item 2", "content", "WORK_LOG",
                                LocalDate.of(2026, 5, 15), null, null,
                                false, new LinkedHashSet<>()
                        )
                ));

        PageResponse<PublicTimelineItemResponse> response = publicTimelineQueryService.getPublicTimeline(
                null, null, null, null, 1, 2
        );

        assertThat(response.totalElements()).isEqualTo(4);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();
        assertThat(response.content()).extracting(PublicTimelineItemResponse::title)
                .containsExactly("Manual item 1", "Manual item 2");
    }
}
