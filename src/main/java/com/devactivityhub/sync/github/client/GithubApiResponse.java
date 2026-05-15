package com.devactivityhub.sync.github.client;

public record GithubApiResponse<T>(
        T body,
        GithubRateLimitSnapshot rateLimitSnapshot
) {
}
