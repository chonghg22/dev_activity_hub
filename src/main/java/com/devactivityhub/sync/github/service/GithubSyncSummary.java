package com.devactivityhub.sync.github.service;

public record GithubSyncSummary(
        long jobId,
        long executionId,
        int commitsSaved,
        int issueActivitiesSaved,
        int pullRequestActivitiesSaved,
        int totalSaved,
        String cursorValue
) {
}
