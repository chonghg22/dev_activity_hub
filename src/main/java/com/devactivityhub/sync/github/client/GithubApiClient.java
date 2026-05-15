package com.devactivityhub.sync.github.client;

import com.devactivityhub.sync.github.dto.GithubCommitResponse;
import com.devactivityhub.sync.github.dto.GithubIssueResponse;
import com.devactivityhub.sync.github.dto.GithubPullRequestResponse;
import com.devactivityhub.sync.github.dto.GithubRepositoryResponse;

import java.time.OffsetDateTime;

public interface GithubApiClient {

    GithubApiResponse<GithubRepositoryResponse> fetchRepository(String owner, String repository);

    GithubFetchResult<GithubCommitResponse> fetchCommits(String owner, String repository, OffsetDateTime since);

    GithubFetchResult<GithubIssueResponse> fetchIssues(String owner, String repository, OffsetDateTime since);

    GithubFetchResult<GithubPullRequestResponse> fetchPullRequests(String owner, String repository, OffsetDateTime since);
}
