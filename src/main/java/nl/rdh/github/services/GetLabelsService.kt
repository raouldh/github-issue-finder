package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.api.v1.model.toSummary
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Repository
import nl.rdh.github.client.model.labelNames
import nl.rdh.github.flatMapParallel
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {
    fun getLabelsForOrg(org: String): List<String> = fetchReposForOrg(org)
        .flatMapParallel { getLabelsForRepo(org, it.name) }

    fun getLabelsForRepo(org: String, repo: String) =
        fetchLabelsForRepo(org, repo)
            .labelNames()

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> = fetchReposForOrg(org)
        .also { println("Repos: " + it.size) }
        .filter { it.has_issues }
        .also { println("Repos with issues: " + it.size) }
        .flatMapParallel { repository -> fetchIssuesForRepo(repository, org) }
        .also { println("IssueCount: " + it.size) }
        .filter { it.isOpenForContribution }
        .also { println("IssueCount.isOpenForContribution: " + it.size) }
        .map { it.toSummary() }

    private fun fetchReposForOrg(org: String) = GithubPagedRequest(
        { githubClient.getAllReposForOrg(org) },
        { githubClient.getAllReposForOrg(org, it) }
    ).execute()

    private fun fetchLabelsForRepo(org: String, repository: String) = GithubPagedRequest(
        { githubClient.getLabelsForRepo(org, repository) },
        { githubClient.getLabelsForRepo(org, repository, it) }
    ).execute()


    private fun fetchIssuesForRepo(repository: Repository, org: String) = GithubPagedRequest(
        { githubClient.getIssuesForRepo(org, repository.name) },
        { githubClient.getIssuesForRepoForPage(org, repository.name, it) }
    ).execute()
}