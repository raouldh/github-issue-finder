package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.api.v1.model.toSummary
import nl.rdh.github.bodyAsList
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Repository
import nl.rdh.github.client.model.labelNames
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    fun getLabelsForOrg(org: String): List<String> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .parallelStream()
        .flatMap { fetchAllLabelsForRepository(it).stream() }
        .distinct()
        .sorted()
        .toList()

    fun getLabelsForRepo(org: String, repo: String) = githubClient
        .getLabelsForRepo(org, repo)
        .bodyAsList()
        .labelNames()


    private fun fetchAllLabelsForRepository(repository: Repository): List<String> {
        val firstPageResponse = githubClient.getLabelsForUrl(repository.labels_url)
        val firstPageLabels = firstPageResponse
            .bodyAsList()

        val additionalPages = firstPageResponse
            .toGithubResponse()
            .flatMapAdditionalPages { page ->
                githubClient
                    .getLabelsForUrlAndPage(repository.labels_url, page)
                    .bodyAsList()
            }

        return (firstPageLabels + additionalPages).labelNames()
    }

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .filter { it.has_issues }
        .map { it.name }
        .parallelStream()
        .flatMap { repoName -> fetchIssuesForRepo(org, repoName).stream() }
        .toList()

    private fun fetchIssuesForRepo(org: String, repoName: String): List<IssueSummary> {
        val firstPageResponse = githubClient.getIssuesForRepo(org, repoName)
        val firstPageIssues = firstPageResponse
            .bodyAsList()
            .filter { it.isOpenForContribution }
            .map { issue -> issue.toSummary() }

        val additionalPages = firstPageResponse
            .toGithubResponse()
            .mapAdditionalPage { page ->
                githubClient.getIssuesForRepoForPage(org, repoName, page)
                    .bodyAsList()
                    .filter { it.isOpenForContribution }
                    .map { it.toSummary() }
            }
            .flatten()

        return firstPageIssues + additionalPages
    }
}