package com.devactivityhub.projectsource.service;

import com.devactivityhub.common.error.DuplicateResourceException;
import com.devactivityhub.project.domain.Project;
import com.devactivityhub.project.domain.ProjectStatus;
import com.devactivityhub.project.domain.ProjectVisibility;
import com.devactivityhub.project.service.ProjectService;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.dto.ProjectSourceCreateRequest;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.job.service.SyncJobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectSourceServiceTest {

    @Mock
    private ProjectSourceRepository projectSourceRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private SyncJobService syncJobService;

    @InjectMocks
    private ProjectSourceService projectSourceService;

    @Test
    void createProjectSourceRejectsDuplicateExternalSource() {
        ProjectSourceCreateRequest request = new ProjectSourceCreateRequest(
                1L,
                ProjectSourceType.GITHUB,
                "101",
                "chonghg22/dev_activity_hub",
                "https://github.com/chonghg22/dev_activity_hub",
                true
        );

        when(projectSourceRepository.existsBySourceTypeAndExternalSourceId(ProjectSourceType.GITHUB, "101")).thenReturn(true);

        assertThatThrownBy(() -> projectSourceService.createProjectSource(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createProjectSourceClearsExistingPrimaryAndCreatesSyncJob() {
        Project project = new Project(
                "Dev Activity Hub",
                "dev-activity-hub",
                "portfolio backend",
                "PORTFOLIO",
                ProjectVisibility.PRIVATE,
                false,
                ProjectStatus.ACTIVE,
                LocalDate.of(2026, 5, 15),
                null
        );
        ReflectionTestUtils.setField(project, "id", 1L);
        ProjectSource existingPrimary = new ProjectSource(
                project,
                ProjectSourceType.GITHUB,
                "100",
                "chonghg22/legacy",
                "https://github.com/chonghg22/legacy",
                true
        );
        ReflectionTestUtils.setField(existingPrimary, "id", 10L);
        ProjectSourceCreateRequest request = new ProjectSourceCreateRequest(
                1L,
                ProjectSourceType.GITHUB,
                "101",
                "chonghg22/dev_activity_hub",
                "https://github.com/chonghg22/dev_activity_hub",
                true
        );

        when(projectSourceRepository.existsBySourceTypeAndExternalSourceId(ProjectSourceType.GITHUB, "101")).thenReturn(false);
        when(projectService.getProjectEntity(1L)).thenReturn(project);
        when(projectSourceRepository.save(any(ProjectSource.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectSourceRepository.findByProjectIdAndSourceType(1L, ProjectSourceType.GITHUB)).thenReturn(List.of(existingPrimary));
        doNothing().when(syncJobService).ensureSyncJob(any(ProjectSource.class));

        projectSourceService.createProjectSource(request);

        verify(syncJobService).ensureSyncJob(any(ProjectSource.class));
        org.assertj.core.api.Assertions.assertThat(existingPrimary.isPrimary()).isFalse();
    }

    @Test
    void getProjectSourceEntityThrowsWhenMissing() {
        when(projectSourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectSourceService.getProjectSourceEntity(99L))
                .isInstanceOf(com.devactivityhub.common.error.ResourceNotFoundException.class);
    }
}
