package com.devactivityhub.config.database;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JpaSchemaProperties.class)
public class DatabaseConfig {
}
