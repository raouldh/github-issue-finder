package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.api.v1.model.toSummary
import nl.rdh.github.bodyAsList
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Label
import nl.rdh.github.client.model.Repository
import nl.rdh.github.client.model.distinctLabelNames
import nl.rdh.github.flatMapParallel
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    fun getLabelsForOrg(org: String): List<String> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .flatMapParallel { fetchAllLabelsForRepository(it) }
        .distinctLabelNames()

    fun getLabelsForRepo(org: String, repo: String) = githubClient
        .getLabelsForRepo(org, repo)
        .bodyAsList()
        .distinctLabelNames()

    private fun fetchAllLabelsForRepository(repository: Repository): List<Label> {
        val request = GithubPagedRequest(
            { githubClient.getLabelsForUrl(repository.labels_url) },
            { githubClient.getLabelsForUrlAndPage(repository.labels_url, it) }
        )
        return request.execute()
    }

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .filter { it.has_issues }
        .flatMapParallel { repository -> fetchIssuesForRepo(org, repository.name) }
        .map { it.toSummary() }

    private fun fetchIssuesForRepo(org: String, repoName: String): List<Issue> {
        val request = GithubPagedRequest(
            { githubClient.getIssuesForRepo(org, repoName) },
            { githubClient.getIssuesForRepoForPage(org, repoName, it) }
        )

        return request
            .execute()
            .filter { it.isOpenForContribution }
    }
}