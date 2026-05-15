package com.devactivityhub.sync.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GithubPullRequestResponse(
        Long id,
        Integer number,
        String title,
        String body,
        GithubUserResponse user,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("closed_at") OffsetDateTime closedAt,
        @JsonProperty("merged_at") OffsetDateTime mergedAt
) {
}
