package com.devactivityhub.sync.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserResponse(
        String login,
        @JsonProperty("html_url") String htmlUrl
) {
}
