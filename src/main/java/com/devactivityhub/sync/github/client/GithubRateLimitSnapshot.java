package com.devactivityhub.sync.github.client;

import java.time.OffsetDateTime;

public record GithubRateLimitSnapshot(
        Integer limit,
        Integer remaining,
        OffsetDateTime resetAt
) {
    public boolean isExhausted() {
        return remaining != null && remaining <= 0;
    }
}
