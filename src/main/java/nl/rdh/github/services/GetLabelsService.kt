package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
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
    }

    fun getLabelsForRepo(org: String, repo: String): List<String> {
        return githubClient.getLabelsForRepo(org, repo).body
            ?.map { it.name }
            .orEmpty()
    }

    fun getLabelsForOrg(org: String): List<String> {
        val allRepos = githubClient.getAllReposForOrg(org).body ?: return emptyList()

        val labels = buildSet {
            allRepos.parallelStream().forEach { repository ->
                addAll(fetchAllLabelsForRepository(repository))
            }
        }

        return labels.sorted()
    }

    private fun fetchAllLabelsForRepository(repository: Repository): List<String> {
        val firstPageResponse = githubClient.getLabelsForUrl(repository.labels_url)
        val firstPageLabels = firstPageResponse.body?.map { it.name }.orEmpty()

        val additionalPages = firstPageResponse.headers[LINK_HEADER]?.let { linkHeader ->
            val lastPage = getLastPageNumber(firstPageResponse.headers)
            (2..lastPage).flatMap { page ->
                githubClient.getLabelsForUrlAndPage(repository.labels_url, page).body
                    ?.map { it.name }
                    .orEmpty()
            }
        }.orEmpty()

        return firstPageLabels + additionalPages
    }

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> {
        val reposWithIssues =
            githubClient.getAllReposForOrg(org).body
                ?.filter { it.has_issues }
                ?.map { it.name }
                .orEmpty()

        return reposWithIssues.parallelStream().flatMap { repoName ->
            fetchIssuesForRepo(org, repoName).stream()
        }.toList()
    }

    private fun fetchIssuesForRepo(org: String, repoName: String): List<IssueSummary> {
        val firstPageResponse = githubClient.getIssuesForRepo(org, repoName)
        val firstPageIssues = firstPageResponse.body
            ?.mapNotNull { extractContributionIssue(it) }
            .orEmpty()

        val additionalPages = firstPageResponse.headers[LINK_HEADER]?.let {
            val lastPage = getLastPageNumber(firstPageResponse.headers)
            (2..lastPage).flatMap { page ->
                githubClient.getIssuesForRepoForPage(org, repoName, page).body
                    ?.mapNotNull { extractContributionIssue(it) }
                    .orEmpty()
            }
        }.orEmpty()

        return firstPageIssues + additionalPages
    }

    private fun getLastPageNumber(headers: HttpHeaders): Int {
        return headers.getFirst(LINK_HEADER)
            ?.let { regexExtractLastPageUrl(it) }
            ?.substringAfter(PAGE_PARAM)
            ?.toIntOrNull()
            ?: 1
    }

    private fun extractContributionIssue(issue: Issue): IssueSummary? {
        val labels = issue.labels ?: return null

        val hasContributionLabel = labels.any { label ->
            label.name in OPEN_FOR_CONTRIBUTION_LABELS
        }

        return if (hasContributionLabel) {
            IssueSummary(
                url = issue.url,
                htmlUrl = issue.html_url,
                title = issue.title,
                state = issue.state.orEmpty(),
                comments = issue.comments ?: 0,
                labels = labels.map { it.name }
            )
        } else {
            null
        }
    }

    private fun regexExtractLastPageUrl(linkHeader: String): String? {
        val regex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(linkHeader)?.groupValues?.getOrNull(1)
    }
}