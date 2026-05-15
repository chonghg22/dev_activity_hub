package com.devactivityhub.projectsource.dto;

import com.devactivityhub.projectsource.domain.ProjectSourceType;
import jakarta.validation.constraints.Size;

public record ProjectSourceUpdateRequest(
        Long projectId,
        ProjectSourceType sourceType,
        @Size(max = 120) String externalSourceId,
        @Size(max = 200) String externalName,
        @Size(max = 300) String externalUrl,
        Boolean isPrimary
) {
}
