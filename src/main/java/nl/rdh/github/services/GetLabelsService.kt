package nl.rdh.github.services

import nl.rdh.github.client.GithubClient
import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Repository
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    // This list is compiled from all unique labels for all spring-cloud and spring-projects repo's
    private val openForContributionLabels: Set<String> = setOf(
        "ideal-for-contribution",
        "ideal-for-user-contribution",
        "status: ideal-for-contribution",
        "status: first-timers-only",
        "status/first-timers-only",
        "community contribution",
        "contribution welcome",
        "help wanted",
        "first-timers-only",
        "good first issue",
        "type/help-needed"
    )

    fun getLabelsForRepo(org: String, repo: String): List<String> {
        return githubClient.getLabelsForRepo(org, repo).body?.map { it.name }.orEmpty()
    }

    fun getLabelsForOrg(org: String): List<String> {
        val labels = mutableSetOf<String>()
        githubClient.getAllReposForOrg(org).body?.forEach { repository ->
                collectLabelsFromRepository(repository, labels)
            }

        return labels.sorted()
    }

    private fun collectLabelsFromRepository(repository: Repository, labels: MutableSet<String>) {
        val labelsUrl = githubClient.getLabelsForUrl(repository.labels_url)
        labelsUrl.body?.mapTo(labels) { it.name }

        labelsUrl.headers["Link"]?.let { linkHeader ->
            val lastPageNumber = getLastPageNumber(labelsUrl.headers)
            if (lastPageNumber > 1) {
                (2..lastPageNumber).forEach { currentPage ->
                    githubClient.getLabelsForUrlAndPage(
                        repository.labels_url,
                        currentPage
                    ).body?.mapTo(labels) { it.name }
                }
            }
        }
    }

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> {
        val reposWithIssues =
            githubClient.getAllReposForOrg(org).body?.filter { it.has_issues }
                ?.map { it.name }.orEmpty()

        return reposWithIssues.flatMap { repoName -> filterIssues(org, repoName) }
    }

    private fun filterIssues(org: String, repoName: String): List<IssueSummary> {
        val issues = mutableListOf<IssueSummary>()

        val issuesForRepo = githubClient.getIssuesForRepo(org, repoName)
        issuesForRepo.body?.mapNotNullTo(issues) { issue -> extractContributionIssue(issue) }

        issuesForRepo.headers["Link"]?.let {
            val lastPageNumber = getLastPageNumber(issuesForRepo.headers)
            if (lastPageNumber > 1) {
                (2..lastPageNumber).forEach { currentPage ->
                    githubClient.getIssuesForRepoForPage(
                        org,
                        repoName,
                        currentPage
                    ).body?.mapNotNullTo(issues) { issue -> extractContributionIssue(issue) }
                }
            }
        }

        return issues
    }

    private fun getLastPageNumber(headers: HttpHeaders): Int {
        return headers.getFirst("Link")
            ?.let { regexExtractLast(it) }
            ?.substringAfter("page=")
            ?.toIntOrNull() ?: 1
    }

    // Return IssueSummary? instead of side effects
    private fun extractContributionIssue(issue: Issue): IssueSummary? {
        val hasContributionLabel =
            issue.labels?.any { label -> label.name in openForContributionLabels }
                ?: false

        return if (hasContributionLabel) {
            IssueSummary(
                url = issue.url,
                htmlUrl = issue.html_url,
                title = issue.title,
                state = issue.state.orEmpty(),
                comments = issue.comments ?: 0,
                labels = issue.labels?.map { it.name }.orEmpty()
            )
        } else {
            null
        }
    }

    private fun regexExtractLast(linkHeader: String): String? {
        val regex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(linkHeader)?.groupValues?.getOrNull(1)
    }
}