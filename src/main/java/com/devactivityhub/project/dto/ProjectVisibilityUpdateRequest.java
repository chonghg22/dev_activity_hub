package com.devactivityhub.project.dto;

import jakarta.validation.constraints.NotNull;

public record ProjectVisibilityUpdateRequest(
        @NotNull Boolean isPublic
) {
}
