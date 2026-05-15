package com.devactivityhub.config.database;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.database")
public record JpaSchemaProperties(
        String schema
) {
}
