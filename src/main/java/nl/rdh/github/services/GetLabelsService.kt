package nl.rdh.github.services

import nl.rdh.github.client.GithubClient
import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Label
import nl.rdh.github.client.model.Repository
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Service
class GetLabelsService(private val githubClient: GithubClient) {

    // This list is compiled from all unique labels for all spring-cloud and spring-projects repo's
    private val openForContributionLabels: List<String> = listOf(
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

    fun getLabelsForRepo(org: String, repo: String): MutableList<String>? {
        return githubClient.getLabelsForRepo(org, repo).body?.stream()
            ?.map { l -> l.name }
            ?.toList()
    }

    fun getLabelsForOrg(org: String): List<String> {
        val labels: CopyOnWriteArrayList<String> = CopyOnWriteArrayList<String>()
        githubClient.getAllReposForOrg(org).body?.stream()
            ?.forEach { repository ->
            val labelsUrl = githubClient.getLabelsForUrl(repository.labels_url)
            labelsUrl.body?.stream()
                ?.map { l -> labels.add(l.name) }
                ?.toList()

            if (labelsUrl.headers.containsKey("Link")) {
                val lastPageNumber = getLastPageNumber(labelsUrl.headers)
                if (lastPageNumber > 1) {
                    var currentPage = 1
                    while (currentPage <= lastPageNumber) {
                        currentPage++
                        githubClient.getLabelsForUrlAndPage(repository.labels_url, currentPage).body?.stream()
                            ?.map { l -> labels.add(l.name) }
                            ?.toList()
                    }

                }
            }
        }
        return Collections.unmodifiableList(labels.distinct().sorted())
    }

    fun getIssuesForMarkedForContribution(org: String): MutableList<IssueSummary>? {
        val issueList: CopyOnWriteArrayList<IssueSummary> = CopyOnWriteArrayList<IssueSummary>()
        val reposWithIssues = githubClient.getAllReposForOrg(org).body?.stream()
            ?.filter(Repository::has_issues)
            ?.map { repo -> repo.name }
            ?.toList()

        reposWithIssues?.parallelStream()
            ?.forEach { repoName -> filterIssues(org, repoName, issueList) }
        return issueList
    }

    private fun filterIssues(
        org: String, repoName: String, issueList: CopyOnWriteArrayList<IssueSummary>
    ) {
        val issuesForRepo = githubClient.getIssuesForRepo(org, repoName)
        issuesForRepo.body?.parallelStream()
            ?.forEach { issue -> extractContributionIssues(issue, issueList, issue?.labels) }

        if (issuesForRepo.headers.containsKey("Link")) {
            val lastPageNumber = getLastPageNumber(issuesForRepo.headers)
            if (lastPageNumber > 1) {
                var currentPage = 1
                while (currentPage <= lastPageNumber) {
                    currentPage++
                    val issuesForRepoForPage = githubClient.getIssuesForRepoForPage(org, repoName, currentPage)
                    issuesForRepoForPage.body?.parallelStream()
                        ?.forEach { issue -> extractContributionIssues(issue, issueList, issue?.labels) }
                }

            }
        }
    }

    private fun getLastPageNumber(headers: HttpHeaders): Int {
        val lastPageUrl = regexExtractLast(headers.getFirst("Link"))
        val indexOfPage = lastPageUrl?.indexOf("page=", 0)
        if (indexOfPage != null) {
            return lastPageUrl.substring((indexOfPage + "page=".length), lastPageUrl.length).toInt()

        }
        return 1 // there are no more pages, use page 1
    }

    private fun extractContributionIssues(
        issue: Issue, issueList: CopyOnWriteArrayList<IssueSummary>, labels: List<Label>?
    ) {
        val hasContributionLabel = issue.labels?.stream()?.anyMatch { label ->
            openForContributionLabels.contains(
                label.name
            )
        }
        if (hasContributionLabel == true) {
            issueList.add(labels?.map { label -> label.name }?.let {
                IssueSummary(
                    issue.url, issue.html_url, issue.title, issue.state ?: "", issue.comments ?: 0, it.toList()
                )
            })
        }
    }

    private fun regexExtractLast(linkHeader: String?): String? {
        val regex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
        return linkHeader?.let { regex.find(it)?.groupValues?.getOrNull(1) }
    }
}