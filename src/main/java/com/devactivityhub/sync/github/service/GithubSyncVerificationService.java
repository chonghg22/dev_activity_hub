package com.devactivityhub.sync.github.service;

import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.github.client.GithubApiClient;
import com.devactivityhub.sync.github.client.GithubApiResponse;
import com.devactivityhub.sync.github.config.GithubSyncProperties;
import com.devactivityhub.sync.github.dto.GithubRepositoryResponse;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GithubSyncVerificationService {

    private static final String SOURCE_TYPE = "GITHUB";

    private final SyncJobRepository syncJobRepository;
    private final ProjectSourceRepository projectSourceRepository;
    private final GithubApiClient githubApiClient;
    private final GithubSyncProperties githubSyncProperties;

    public GithubSyncVerificationService(SyncJobRepository syncJobRepository,
                                         ProjectSourceRepository projectSourceRepository,
                                         GithubApiClient githubApiClient,
                                         GithubSyncProperties githubSyncProperties) {
        this.syncJobRepository = syncJobRepository;
        this.projectSourceRepository = projectSourceRepository;
        this.githubApiClient = githubApiClient;
        this.githubSyncProperties = githubSyncProperties;
    }

    public GithubSyncVerificationResult verifyJob(long jobId) {
        SyncJob syncJob = syncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Sync job not found. id=" + jobId));

        ProjectSource projectSource = resolveGithubProjectSource(syncJob);
        GithubRepositoryRef repositoryRef = GithubRepositoryRef.from(projectSource.getExternalName());
        GithubApiResponse<GithubRepositoryResponse> repositoryResponse =
                githubApiClient.fetchRepository(repositoryRef.owner(), repositoryRef.repository());

        GithubRepositoryResponse repository = repositoryResponse.body();

        return new GithubSyncVerificationResult(
                syncJob.getId(),
                repositoryRef.fullName(),
                githubSyncProperties.token() != null && !githubSyncProperties.token().isBlank(),
                repository != null,
                repository == null ? null : repository.htmlUrl(),
                repository != null && repository.privateRepository(),
                repository == null ? null : repository.defaultBranch(),
                repository == null ? null : repository.pushedAt(),
                repositoryResponse.rateLimitSnapshot() == null ? null : repositoryResponse.rateLimitSnapshot().remaining(),
                repositoryResponse.rateLimitSnapshot() == null ? null : repositoryResponse.rateLimitSnapshot().resetAt()
        );
    }

    private ProjectSource resolveGithubProjectSource(SyncJob syncJob) {
        if (!SOURCE_TYPE.equals(syncJob.getSourceType())) {
            throw new IllegalArgumentException("Unsupported sync source type. sourceType=" + syncJob.getSourceType());
        }

        GithubRepositoryRef repositoryRef = GithubRepositoryRef.fromJobName(syncJob.getJobName());
        return projectSourceRepository.findBySourceTypeAndExternalName(ProjectSourceType.GITHUB, repositoryRef.fullName())
                .orElseThrow(() -> new ResourceNotFoundException("Project source not found for sync job. jobId=" + syncJob.getId()));
    }

    private record GithubRepositoryRef(String owner, String repository) {
        private static GithubRepositoryRef from(String externalName) {
            String[] parts = externalName.split("/", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                throw new IllegalArgumentException("GitHub repository name must be in owner/repository format");
            }
            return new GithubRepositoryRef(parts[0], parts[1]);
        }

        private static GithubRepositoryRef fromJobName(String jobName) {
            if (jobName == null || !jobName.startsWith("github:")) {
                throw new IllegalArgumentException("GitHub sync job name must start with github:");
            }
            return from(jobName.substring("github:".length()));
        }

        private String fullName() {
            return owner + "/" + repository;
        }
    }
}
