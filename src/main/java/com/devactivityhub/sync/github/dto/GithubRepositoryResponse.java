package com.devactivityhub.sync.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GithubRepositoryResponse(
        Long id,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("private") boolean privateRepository,
        @JsonProperty("default_branch") String defaultBranch,
        @JsonProperty("pushed_at") OffsetDateTime pushedAt
) {
}
