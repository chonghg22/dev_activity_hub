package com.devactivityhub.sync.github.client;

import java.util.List;

public record GithubFetchResult<T>(
        List<T> items,
        GithubRateLimitSnapshot rateLimitSnapshot
) {
}
