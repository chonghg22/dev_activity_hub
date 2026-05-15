package com.devactivityhub.config.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
    public List<String> resolvedAllowedOrigins() {
        if (allowedOrigins == null) {
            return List.of();
        }

        return allowedOrigins.stream()
                .filter(origin -> origin != null && !origin.isBlank())
                .toList();
    }
}
