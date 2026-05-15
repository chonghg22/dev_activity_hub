package com.devactivityhub.projectsource.dto;

import com.devactivityhub.projectsource.domain.ProjectSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectSourceCreateRequest(
        @NotNull Long projectId,
        @NotNull ProjectSourceType sourceType,
        @NotBlank @Size(max = 120) String externalSourceId,
        @NotBlank @Size(max = 200) String externalName,
        @Size(max = 300) String externalUrl,
        Boolean isPrimary
) {
}
