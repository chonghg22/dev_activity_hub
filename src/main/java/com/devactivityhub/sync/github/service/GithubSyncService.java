package com.devactivityhub.sync.github.service;

import com.devactivityhub.activity.external.domain.ExternalActivity;
import com.devactivityhub.activity.external.repository.ExternalActivityRepository;
import com.devactivityhub.common.error.ResourceNotFoundException;
import com.devactivityhub.projectsource.domain.ProjectSource;
import com.devactivityhub.projectsource.domain.ProjectSourceType;
import com.devactivityhub.projectsource.repository.ProjectSourceRepository;
import com.devactivityhub.sync.execution.domain.SyncExecution;
import com.devactivityhub.sync.execution.service.SyncExecutionService;
import com.devactivityhub.sync.github.client.GithubApiClient;
import com.devactivityhub.sync.github.client.GithubFetchResult;
import com.devactivityhub.sync.github.client.GithubRateLimitExceededException;
import com.devactivityhub.sync.github.config.GithubSyncProperties;
import com.devactivityhub.sync.github.dto.GithubCommitResponse;
import com.devactivityhub.sync.github.dto.GithubIssueResponse;
import com.devactivityhub.sync.github.dto.GithubPullRequestResponse;
import com.devactivityhub.sync.job.domain.SyncJob;
import com.devactivityhub.sync.job.repository.SyncJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GithubSyncService {

    private static final String SOURCE_TYPE = "GITHUB";

    private final SyncJobRepository syncJobRepository;
    private final ProjectSourceRepository projectSourceRepository;
    private final ExternalActivityRepository externalActivityRepository;
    private final SyncExecutionService syncExecutionService;
    private final GithubApiClient githubApiClient;
    private final GithubSyncProperties githubSyncProperties;
    private final ObjectMapper objectMapper;

    public GithubSyncService(SyncJobRepository syncJobRepository,
                             ProjectSourceRepository projectSourceRepository,
                             ExternalActivityRepository externalActivityRepository,
                             SyncExecutionService syncExecutionService,
                             GithubApiClient githubApiClient,
                             GithubSyncProperties githubSyncProperties,
                             ObjectMapper objectMapper) {
        this.syncJobRepository = syncJobRepository;
        this.projectSourceRepository = projectSourceRepository;
        this.externalActivityRepository = externalActivityRepository;
        this.syncExecutionService = syncExecutionService;
        this.githubApiClient = githubApiClient;
        this.githubSyncProperties = githubSyncProperties;
        this.objectMapper = objectMapper;
    }

    public GithubSyncSummary runJob(long jobId, String requestedBy) {
        return runJob(jobId, requestedBy, false);
    }

    public GithubSyncSummary runJob(long jobId, String requestedBy, boolean fullSync) {
        SyncJob syncJob = syncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Sync job not found. id=" + jobId));

        ProjectSource projectSource = resolveGithubProjectSource(syncJob);
        SyncExecution syncExecution = syncExecutionService.start(syncJob, requestedBy);
        syncJob.markRunning();

        try {
            GithubRepositoryRef repositoryRef = GithubRepositoryRef.from(projectSource.getExternalName());
            OffsetDateTime since = fullSync ? null : resolveSince(syncJob.getCursorValue(), syncJob.getLastSyncedAt());

            GithubFetchResult<GithubCommitResponse> commitResult = githubApiClient.fetchCommits(repositoryRef.owner(), repositoryRef.repository(), since);
            verifyRemainingBudget(commitResult, projectSource.getExternalName(), "commit");

            GithubFetchResult<GithubIssueResponse> issueResult = githubApiClient.fetchIssues(repositoryRef.owner(), repositoryRef.repository(), since);
            verifyRemainingBudget(issueResult, projectSource.getExternalName(), "issue");

            GithubFetchResult<GithubPullRequestResponse> pullRequestResult = githubApiClient.fetchPullRequests(repositoryRef.owner(), repositoryRef.repository(), since);

            List<GithubCommitResponse> commits = commitResult.items();
            List<GithubIssueResponse> issues = issueResult.items().stream()
                    .filter(issue -> !issue.isPullRequest())
                    .toList();
            List<GithubPullRequestResponse> pullRequests = pullRequestResult.items();

            int commitsSaved = saveCommitActivities(projectSource, commits);
            int issueActivitiesSaved = saveIssueActivities(projectSource, issues);
            int pullRequestActivitiesSaved = savePullRequestActivities(projectSource, pullRequests);
            OffsetDateTime syncedAt = OffsetDateTime.now();
            OffsetDateTime cursorTime = resolveNextCursor(commits, issues, pullRequests, syncedAt);
            String cursorValue = cursorTime.toString();
            int totalSaved = commitsSaved + issueActivitiesSaved + pullRequestActivitiesSaved;
            String resultSummary = "Saved " + totalSaved + " activities from " + projectSource.getExternalName();

            syncJob.markSuccess(syncedAt, cursorValue);
            syncExecutionService.markSuccess(syncExecution, resultSummary);

            return new GithubSyncSummary(
                    syncJob.getId(),
                    syncExecution.getId(),
                    commitsSaved,
                    issueActivitiesSaved,
                    pullRequestActivitiesSaved,
                    totalSaved,
                    cursorValue
            );
        } catch (RuntimeException exception) {
            syncJob.markFailure();
            syncExecutionService.markFailure(syncExecution, exception.getMessage());
            throw exception;
        }
    }

    private ProjectSource resolveGithubProjectSource(SyncJob syncJob) {
        if (!SOURCE_TYPE.equals(syncJob.getSourceType())) {
            throw new IllegalArgumentException("Unsupported sync source type. sourceType=" + syncJob.getSourceType());
        }

        GithubRepositoryRef repositoryRef = GithubRepositoryRef.fromJobName(syncJob.getJobName());
        return projectSourceRepository.findBySourceTypeAndExternalName(ProjectSourceType.GITHUB, repositoryRef.fullName())
                .orElseThrow(() -> new ResourceNotFoundException("Project source not found for sync job. jobId=" + syncJob.getId()));
    }

    private OffsetDateTime resolveSince(String cursorValue, OffsetDateTime lastSyncedAt) {
        OffsetDateTime baseTime = null;
        if (cursorValue != null && !cursorValue.isBlank()) {
            baseTime = OffsetDateTime.parse(cursorValue);
        } else if (lastSyncedAt != null) {
            baseTime = lastSyncedAt;
        }
        if (baseTime == null) {
            return null;
        }
        return baseTime.minusMinutes(githubSyncProperties.resolvedCursorOverlapMinutes());
    }

    private void verifyRemainingBudget(GithubFetchResult<?> result, String repositoryName, String resourceType) {
        if (result.rateLimitSnapshot() != null && result.rateLimitSnapshot().isExhausted()) {
            throw new GithubRateLimitExceededException(
                    "GitHub API rate limit exhausted after " + resourceType + " fetch for repository "
                            + repositoryName
                            + (result.rateLimitSnapshot().resetAt() == null ? "" : ". Retry after " + result.rateLimitSnapshot().resetAt()),
                    result.rateLimitSnapshot().resetAt()
            );
        }
    }

    private OffsetDateTime resolveNextCursor(List<GithubCommitResponse> commits,
                                             List<GithubIssueResponse> issues,
                                             List<GithubPullRequestResponse> pullRequests,
                                             OffsetDateTime fallback) {
        List<OffsetDateTime> timestamps = new ArrayList<>();

        commits.stream()
                .map(commit -> commit.commit() == null || commit.commit().author() == null ? null : commit.commit().author().date())
                .filter(java.util.Objects::nonNull)
                .forEach(timestamps::add);

        issues.stream()
                .map(this::resolveIssueCursorTime)
                .filter(java.util.Objects::nonNull)
                .forEach(timestamps::add);

        pullRequests.stream()
                .map(this::resolvePullRequestCursorTime)
                .filter(java.util.Objects::nonNull)
                .forEach(timestamps::add);

        return timestamps.stream()
                .max(OffsetDateTime::compareTo)
                .orElse(fallback);
    }

    private OffsetDateTime resolveIssueCursorTime(GithubIssueResponse issue) {
        OffsetDateTime latest = issue.createdAt();
        if (issue.closedAt() != null && (latest == null || issue.closedAt().isAfter(latest))) {
            latest = issue.closedAt();
        }
        return latest;
    }

    private OffsetDateTime resolvePullRequestCursorTime(GithubPullRequestResponse pullRequest) {
        OffsetDateTime latest = pullRequest.createdAt();
        if (pullRequest.closedAt() != null && (latest == null || pullRequest.closedAt().isAfter(latest))) {
            latest = pullRequest.closedAt();
        }
        if (pullRequest.mergedAt() != null && (latest == null || pullRequest.mergedAt().isAfter(latest))) {
            latest = pullRequest.mergedAt();
        }
        return latest;
    }

    private int saveCommitActivities(ProjectSource projectSource, List<GithubCommitResponse> commits) {
        int savedCount = 0;
        for (GithubCommitResponse commit : commits) {
            String sourceId = "commit:" + commit.sha();
            savedCount += saveExternalActivityIfAbsent(
                    sourceId,
                    new ExternalActivity(
                            projectSource.getProject(),
                            projectSource,
                            SOURCE_TYPE,
                            sourceId,
                            "COMMIT",
                            firstLine(commit.commit().message()),
                            commit.commit().message(),
                            resolveCommitActor(commit),
                            commit.htmlUrl(),
                            commit.commit().author().date(),
                            projectSource.getProject().isPublic(),
                            objectMapper.valueToTree(commit)
                    )
            );
        }
        return savedCount;
    }

    private int saveIssueActivities(ProjectSource projectSource, List<GithubIssueResponse> issues) {
        int savedCount = 0;
        for (GithubIssueResponse issue : issues) {
            savedCount += saveExternalActivityIfAbsent(
                    "issue-opened:" + issue.id(),
                    new ExternalActivity(
                            projectSource.getProject(),
                            projectSource,
                            SOURCE_TYPE,
                            "issue-opened:" + issue.id(),
                            "ISSUE_OPENED",
                            "#" + issue.number() + " " + issue.title(),
                            issue.body(),
                            issue.user() == null ? null : issue.user().login(),
                            issue.htmlUrl(),
                            issue.createdAt(),
                            projectSource.getProject().isPublic(),
                            objectMapper.valueToTree(issue)
                    )
            );

            if (issue.closedAt() != null) {
                savedCount += saveExternalActivityIfAbsent(
                        "issue-closed:" + issue.id(),
                        new ExternalActivity(
                                projectSource.getProject(),
                                projectSource,
                                SOURCE_TYPE,
                                "issue-closed:" + issue.id(),
                                "ISSUE_CLOSED",
                                "#" + issue.number() + " " + issue.title(),
                                issue.body(),
                                issue.user() == null ? null : issue.user().login(),
                                issue.htmlUrl(),
                                issue.closedAt(),
                                projectSource.getProject().isPublic(),
                                objectMapper.valueToTree(issue)
                        )
                );
            }
        }
        return savedCount;
    }

    private int savePullRequestActivities(ProjectSource projectSource, List<GithubPullRequestResponse> pullRequests) {
        int savedCount = 0;
        for (GithubPullRequestResponse pullRequest : pullRequests) {
            savedCount += saveExternalActivityIfAbsent(
                    "pr-opened:" + pullRequest.id(),
                    new ExternalActivity(
                            projectSource.getProject(),
                            projectSource,
                            SOURCE_TYPE,
                            "pr-opened:" + pullRequest.id(),
                            "PR_OPENED",
                            "#" + pullRequest.number() + " " + pullRequest.title(),
                            pullRequest.body(),
                            pullRequest.user() == null ? null : pullRequest.user().login(),
                            pullRequest.htmlUrl(),
                            pullRequest.createdAt(),
                            projectSource.getProject().isPublic(),
                            objectMapper.valueToTree(pullRequest)
                    )
            );

            if (pullRequest.mergedAt() != null) {
                savedCount += saveExternalActivityIfAbsent(
                        "pr-merged:" + pullRequest.id(),
                        new ExternalActivity(
                                projectSource.getProject(),
                                projectSource,
                                SOURCE_TYPE,
                                "pr-merged:" + pullRequest.id(),
                                "PR_MERGED",
                                "#" + pullRequest.number() + " " + pullRequest.title(),
                                pullRequest.body(),
                                pullRequest.user() == null ? null : pullRequest.user().login(),
                                pullRequest.htmlUrl(),
                                pullRequest.mergedAt(),
                                projectSource.getProject().isPublic(),
                                objectMapper.valueToTree(pullRequest)
                        )
                );
                continue;
            }

            if (pullRequest.closedAt() != null) {
                savedCount += saveExternalActivityIfAbsent(
                        "pr-closed:" + pullRequest.id(),
                        new ExternalActivity(
                                projectSource.getProject(),
                                projectSource,
                                SOURCE_TYPE,
                                "pr-closed:" + pullRequest.id(),
                                "PR_CLOSED",
                                "#" + pullRequest.number() + " " + pullRequest.title(),
                                pullRequest.body(),
                                pullRequest.user() == null ? null : pullRequest.user().login(),
                                pullRequest.htmlUrl(),
                                pullRequest.closedAt(),
                                projectSource.getProject().isPublic(),
                                objectMapper.valueToTree(pullRequest)
                        )
                );
            }
        }
        return savedCount;
    }

    private int saveExternalActivityIfAbsent(String sourceId, ExternalActivity externalActivity) {
        if (externalActivityRepository.findBySourceTypeAndSourceId(SOURCE_TYPE, sourceId).isPresent()) {
            return 0;
        }
        externalActivityRepository.save(externalActivity);
        return 1;
    }

    private String resolveCommitActor(GithubCommitResponse commit) {
        if (commit.author() != null && commit.author().login() != null) {
            return commit.author().login();
        }
        return commit.commit().author().name();
    }

    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "Untitled activity";
        }
        return message.lines().findFirst().orElse(message);
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
