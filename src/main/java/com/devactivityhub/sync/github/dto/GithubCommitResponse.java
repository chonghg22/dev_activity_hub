package com.devactivityhub.sync.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GithubCommitResponse(
        String sha,
        GithubUserResponse author,
        GithubCommitDetail commit,
        @JsonProperty("html_url") String htmlUrl
) {
    public record GithubCommitDetail(
            GithubCommitAuthor author,
            String message
    ) {
    }

    public record GithubCommitAuthor(
            String name,
            String email,
            OffsetDateTime date
    ) {
    }
}
