package com.devactivityhub.sync.github.client;

import java.time.OffsetDateTime;

public class GithubRateLimitExceededException extends RuntimeException {

    private final OffsetDateTime resetAt;

    public GithubRateLimitExceededException(String message, OffsetDateTime resetAt) {
        super(message);
        this.resetAt = resetAt;
    }

    public OffsetDateTime getResetAt() {
        return resetAt;
    }
}
