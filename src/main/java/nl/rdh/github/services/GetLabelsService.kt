package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.bodyAsList
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Repository
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    private companion object {
        const val LINK_HEADER = "Link"
        const val PAGE_PARAM = "page="

        // This list is compiled from all unique labels for all spring-cloud and spring-projects repo's
        val OPEN_FOR_CONTRIBUTION_LABELS = setOf(
            "ideal-for-contribution",
            "ideal-for-user-contribution",
            "status: ideal-for-contribution",
            "status: need-help-to-reproduce",
            "status: first-timers-only",
            "status/first-timers-only",
            "community contribution",
            "contribution welcome",
            "help wanted",
            "first-timers-only",
            "good first issue",
            "type/help-needed"
        )

        val lastPageUrlRegex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
    }

    fun getLabelsForRepo(org: String, repo: String) = githubClient
        .getLabelsForRepo(org, repo)
        .bodyAsList()
        .map { it.name }

    fun getLabelsForOrg(org: String) = githubClient
        .getAllReposForOrg(org)
        .bodyAsList()
        .parallelStream()
        .flatMap { fetchAllLabelsForRepository(it).stream() }
        .sorted()
        .toList()

    private fun fetchAllLabelsForRepository(repository: Repository): List<String> {
        val firstPageResponse = githubClient.getLabelsForUrl(repository.labels_url)
        val firstPageLabels = firstPageResponse.bodyAsList().map { it.name }

        val additionalPages = firstPageResponse.headers[LINK_HEADER]
            ?.let { linkHeader ->
                val lastPage = getLastPageNumber(firstPageResponse.headers)
                (2..lastPage)
                    .flatMap { page ->
                        githubClient
                            .getLabelsForUrlAndPage(repository.labels_url, page)
                            .bodyAsList()
                            .map { it.name }
                    }
            }
            .orEmpty()

        return firstPageLabels + additionalPages
    }

    fun getIssuesForMarkedForContribution(org: String) = githubClient
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
            .filter {
                it.labels
                    .orEmpty()
                    .any { label -> label.name in OPEN_FOR_CONTRIBUTION_LABELS }
            }
            .map { issue ->
                IssueSummary(
                    url = issue.url,
                    htmlUrl = issue.html_url,
                    title = issue.title,
                    state = issue.state.orEmpty(),
                    comments = issue.comments ?: 0,
                    labels = issue.labels.orEmpty().map { it.name }
                )
            }

        val additionalPages = firstPageResponse.headers[LINK_HEADER]
            ?.let {
                val lastPage = getLastPageNumber(firstPageResponse.headers)
                (2..lastPage)
                    .flatMap { page ->
                        githubClient.getIssuesForRepoForPage(org, repoName, page)
                            .bodyAsList()
                            .mapNotNull { extractContributionIssue(it) }
                    }
            }
            .orEmpty()

        return firstPageIssues + additionalPages
    }

    private fun getLastPageNumber(headers: HttpHeaders) = headers
        .getFirst(LINK_HEADER)
        ?.let { regexExtractLastPageUrl(it) }
        ?.substringAfter(PAGE_PARAM)
        ?.toIntOrNull()
        ?: 1

    private fun extractContributionIssue(issue: Issue): IssueSummary? {
        val labels = issue.labels ?: emptyList()

        val hasContributionLabel = labels.any { label ->
            label.name in OPEN_FOR_CONTRIBUTION_LABELS
        }

        return hasContributionLabel
            .takeIf { it }
            ?.let {
                IssueSummary(
                    url = issue.url,
                    htmlUrl = issue.html_url,
                    title = issue.title,
                    state = issue.state.orEmpty(),
                    comments = issue.comments ?: 0,
                    labels = labels.map { it.name }
                )
            }
    }

    private fun regexExtractLastPageUrl(linkHeader: String) = lastPageUrlRegex
        .find(linkHeader)
        ?.groupValues
        ?.getOrNull(1)

}