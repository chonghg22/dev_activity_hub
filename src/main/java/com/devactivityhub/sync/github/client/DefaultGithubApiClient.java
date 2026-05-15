package com.devactivityhub.sync.github.client;

import com.devactivityhub.sync.github.config.GithubSyncProperties;
import com.devactivityhub.sync.github.dto.GithubCommitResponse;
import com.devactivityhub.sync.github.dto.GithubIssueResponse;
import com.devactivityhub.sync.github.dto.GithubPullRequestResponse;
import com.devactivityhub.sync.github.dto.GithubRepositoryResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DefaultGithubApiClient implements GithubApiClient {

    private final RestClient restClient;
    private final GithubSyncProperties githubSyncProperties;

    public DefaultGithubApiClient(RestClient.Builder restClientBuilder,
                                  GithubSyncProperties githubSyncProperties) {
        this.githubSyncProperties = githubSyncProperties;
        this.restClient = restClientBuilder
                .baseUrl(githubSyncProperties.resolvedApiBaseUrl())
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    headers.add("X-GitHub-Api-Version", "2022-11-28");
                    if (githubSyncProperties.token() != null && !githubSyncProperties.token().isBlank()) {
                        headers.setBearerAuth(githubSyncProperties.token());
                    }
                })
                .build();
    }

    @Override
    public GithubApiResponse<GithubRepositoryResponse> fetchRepository(String owner, String repository) {
        ResponseEntity<GithubRepositoryResponse> response = restClient.get()
                .uri("/repos/{owner}/{repo}", owner, repository)
                .retrieve()
                .toEntity(GithubRepositoryResponse.class);

        return new GithubApiResponse<>(response.getBody(), toRateLimitSnapshot(response));
    }

    @Override
    public GithubFetchResult<GithubCommitResponse> fetchCommits(String owner, String repository, OffsetDateTime since) {
        List<GithubCommitResponse> items = new java.util.ArrayList<>();
        GithubRateLimitSnapshot snapshot = null;
        int perPage = githubSyncProperties.resolvedPerPage();

        for (int page = 1; page <= githubSyncProperties.resolvedMaxPages(); page++) {
            int currentPage = page;
            ResponseEntity<GithubCommitResponse[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/commits")
                            .queryParam("per_page", perPage)
                            .queryParam("page", currentPage)
                            .queryParamIfPresent("since", java.util.Optional.ofNullable(since))
                            .build(owner, repository))
                    .retrieve()
                    .toEntity(GithubCommitResponse[].class);

            List<GithubCommitResponse> pageItems = toList(response.getBody());
            items.addAll(pageItems);
            snapshot = toRateLimitSnapshot(response);

            if (pageItems.size() < perPage) {
                break;
            }
            verifyPageContinuation(snapshot, owner, repository);
        }

        return new GithubFetchResult<>(items, snapshot);
    }

    @Override
    public GithubFetchResult<GithubIssueResponse> fetchIssues(String owner, String repository, OffsetDateTime since) {
        List<GithubIssueResponse> items = new java.util.ArrayList<>();
        GithubRateLimitSnapshot snapshot = null;
        int perPage = githubSyncProperties.resolvedPerPage();

        for (int page = 1; page <= githubSyncProperties.resolvedMaxPages(); page++) {
            int currentPage = page;
            ResponseEntity<GithubIssueResponse[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/issues")
                            .queryParam("state", "all")
                            .queryParam("sort", "updated")
                            .queryParam("direction", "desc")
                            .queryParam("per_page", perPage)
                            .queryParam("page", currentPage)
                            .queryParamIfPresent("since", java.util.Optional.ofNullable(since))
                            .build(owner, repository))
                    .retrieve()
                    .toEntity(GithubIssueResponse[].class);

            List<GithubIssueResponse> pageItems = toList(response.getBody());
            items.addAll(pageItems);
            snapshot = toRateLimitSnapshot(response);

            if (pageItems.size() < perPage) {
                break;
            }
            verifyPageContinuation(snapshot, owner, repository);
        }

        return new GithubFetchResult<>(items, snapshot);
    }

    @Override
    public GithubFetchResult<GithubPullRequestResponse> fetchPullRequests(String owner, String repository, OffsetDateTime since) {
        List<GithubPullRequestResponse> items = new java.util.ArrayList<>();
        GithubRateLimitSnapshot snapshot = null;
        int perPage = githubSyncProperties.resolvedPerPage();

        for (int page = 1; page <= githubSyncProperties.resolvedMaxPages(); page++) {
            int currentPage = page;
            ResponseEntity<GithubPullRequestResponse[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/pulls")
                            .queryParam("state", "all")
                            .queryParam("sort", "updated")
                            .queryParam("direction", "desc")
                            .queryParam("per_page", perPage)
                            .queryParam("page", currentPage)
                            .build(owner, repository))
                    .retrieve()
                    .toEntity(GithubPullRequestResponse[].class);

            List<GithubPullRequestResponse> pageItems = toList(response.getBody()).stream()
                    .filter(pullRequest -> since == null
                            || pullRequest.createdAt() != null && !pullRequest.createdAt().isBefore(since)
                            || pullRequest.closedAt() != null && !pullRequest.closedAt().isBefore(since)
                            || pullRequest.mergedAt() != null && !pullRequest.mergedAt().isBefore(since))
                    .toList();
            items.addAll(pageItems);
            snapshot = toRateLimitSnapshot(response);

            if (response.getBody() == null || response.getBody().length < perPage) {
                break;
            }
            verifyPageContinuation(snapshot, owner, repository);
        }

        return new GithubFetchResult<>(items, snapshot);
    }

    private <T> List<T> toList(T[] body) {
        return body == null ? List.of() : Arrays.asList(body);
    }

    private GithubRateLimitSnapshot toRateLimitSnapshot(ResponseEntity<?> response) {
        Integer limit = parseInteger(response.getHeaders().getFirst("X-RateLimit-Limit"));
        Integer remaining = parseInteger(response.getHeaders().getFirst("X-RateLimit-Remaining"));
        OffsetDateTime resetAt = parseResetAt(response.getHeaders().getFirst("X-RateLimit-Reset"));
        return new GithubRateLimitSnapshot(limit, remaining, resetAt);
    }

    private void verifyPageContinuation(GithubRateLimitSnapshot snapshot, String owner, String repository) {
        if (snapshot != null && snapshot.isExhausted()) {
            throw new GithubRateLimitExceededException(
                    "GitHub API rate limit exhausted for repository " + owner + "/" + repository
                            + (snapshot.resetAt() == null ? "" : ". Retry after " + snapshot.resetAt()),
                    snapshot.resetAt()
            );
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private OffsetDateTime parseResetAt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(value)), java.time.ZoneOffset.UTC);
    }
}
