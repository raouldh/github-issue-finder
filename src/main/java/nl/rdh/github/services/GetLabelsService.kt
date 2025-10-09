package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.api.v1.model.toSummary
import nl.rdh.github.bodyAsList
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Repository
import nl.rdh.github.client.model.labelNames
import nl.rdh.github.flatMapParallel
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    fun getLabelsForOrg(org: String): List<String> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .flatMapParallel { fetchLabelsForRepo(it) }
        .labelNames()

    fun getLabelsForRepo(org: String, repo: String) = githubClient
        .getLabelsForRepo(org, repo) // TODO [MvdB]: Deze haalt alleen de eerste pagina op
        .bodyAsList()
        .labelNames()

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .filter { it.has_issues }
        .flatMapParallel { repository -> fetchIssuesForRepo(repository, org) }
        .filter { it.isOpenForContribution }
        .map { it.toSummary() }

    private fun fetchLabelsForRepo(repository: Repository) = GithubPagedRequest(
        { githubClient.getLabelsForUrl(repository.labels_url) },
        { githubClient.getLabelsForUrlAndPage(repository.labels_url, it) }
    ).execute()

    private fun fetchIssuesForRepo(repository: Repository, org: String) = GithubPagedRequest(
        { githubClient.getIssuesForRepo(org, repository.name) },
        { githubClient.getIssuesForRepoForPage(org, repository.name, it) }
    ).execute()
}