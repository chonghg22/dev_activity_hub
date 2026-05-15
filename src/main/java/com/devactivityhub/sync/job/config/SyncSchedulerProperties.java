package com.devactivityhub.sync.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sync.scheduler")
public record SyncSchedulerProperties(
        Boolean enabled,
        Long fixedDelayMs,
        Integer hourlyIntervalMinutes,
        Boolean autoUpgradeLegacyManualJobs
) {
    public boolean resolvedEnabled() {
        return enabled == null || enabled;
    }

    public long resolvedFixedDelayMs() {
        return fixedDelayMs == null || fixedDelayMs < 1000 ? 300_000L : fixedDelayMs;
    }

    public int resolvedHourlyIntervalMinutes() {
        return hourlyIntervalMinutes == null || hourlyIntervalMinutes < 1 ? 60 : hourlyIntervalMinutes;
    }

    public boolean resolvedAutoUpgradeLegacyManualJobs() {
        return autoUpgradeLegacyManualJobs == null || autoUpgradeLegacyManualJobs;
    }
}
