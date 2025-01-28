package nl.rdh.github.services

import nl.rdh.github.api.v1.GithubClient
import nl.rdh.github.api.v1.IssueSummary
import nl.rdh.github.model.Repository
import org.springframework.stereotype.Service
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList

@Service
class GetLabelsService(private val githubClient: GithubClient) {
    private val openForContributionLabels: List<String> = listOf(
        "ideal-for-contribution",
        "status: ideal-for-contribution",
        "community contribution",
        "help wanted",
        "first-timers-only",
        "good first issue"
    )

    fun getLabelsForRepo(owner: String, repo: String): MutableList<String>? {
        return githubClient.getLabelsForRepo(owner, repo).body
            ?.stream()
            ?.map { l -> l.name }
            ?.toList()
    }

    fun getLabelsForOrg(org: String): List<String> {
        val labels: CopyOnWriteArrayList<String> = CopyOnWriteArrayList<String>()
        githubClient.getAllReposForOrg(org).body?.stream()?.forEach { repository ->
            githubClient.getLabelsUrl(repository.labels_url).body
                ?.stream()
                ?.map { l -> labels.add(l.name) }
                ?.toList()
        }
        return Collections.unmodifiableList(labels)
    }

    // TODO: is there a better way to get issues for label?
    fun getIssuesForMarkedForContribution(org: String): MutableList<IssueSummary>? {
        val issueList: CopyOnWriteArrayList<IssueSummary> = CopyOnWriteArrayList<IssueSummary>()
        val reposWithIssues = githubClient.getAllReposForOrg(org).body?.stream()
            ?.filter(Repository::has_issues)
            ?.map { repo -> repo.name }
            ?.toList()

        reposWithIssues?.forEach { repoName ->
            filterIssues(org, repoName, issueList)
        }
//        reposWithIssues?.stream()?.parallel()?.map { repo -> filterIssues(org, repo, issueList) }

        return issueList
    }

    private fun filterIssues(
        org: String,
        repoName: String,
        issueList: CopyOnWriteArrayList<IssueSummary>
    ) {
        val issuesForRepo = githubClient.getIssuesForRepo(org, repoName)
        issuesForRepo.body?.stream()?.forEach { issue ->
            val hasContributionLabel = issue.labels?.stream()?.anyMatch { label ->
                openForContributionLabels.contains(
                    label.name
                )
            }
            if (hasContributionLabel == true) {
                issueList.add(
                    IssueSummary(
                        issue?.url ?: "",
                        issue?.title ?: "",
                        issue?.state ?: "",
                        issue?.comments ?: 0,
                        issue?.labels.map { label -> label.name }.toList()
                    )
                )
            }
        }
    }

}