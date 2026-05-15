package com.devactivityhub.sync.github.service;

import java.time.OffsetDateTime;

public record GithubSyncVerificationResult(
        long jobId,
        String repository,
        boolean tokenConfigured,
        boolean repositoryAccessible,
        String repositoryUrl,
        boolean privateRepository,
        String defaultBranch,
        OffsetDateTime pushedAt,
        Integer rateLimitRemaining,
        OffsetDateTime rateLimitResetAt
) {
}
