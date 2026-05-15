package com.devactivityhub.sync.github.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.github")
public record GithubSyncProperties(
        String apiBaseUrl,
        String token,
        Integer perPage,
        Integer maxPages,
        Integer cursorOverlapMinutes
) {
    public String resolvedApiBaseUrl() {
        return apiBaseUrl == null || apiBaseUrl.isBlank() ? "https://api.github.com" : apiBaseUrl;
    }

    public int resolvedPerPage() {
        return perPage == null || perPage < 1 ? 100 : perPage;
    }

    public int resolvedMaxPages() {
        return maxPages == null || maxPages < 1 ? 10 : maxPages;
    }

    public int resolvedCursorOverlapMinutes() {
        return cursorOverlapMinutes == null || cursorOverlapMinutes < 0 ? 5 : cursorOverlapMinutes;
    }
}
