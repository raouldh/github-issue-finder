package nl.rdh.github.services

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.api.v1.model.toSummary
import nl.rdh.github.client.model.labelNames
import nl.rdh.github.extensions.flatMapParallel
import org.springframework.stereotype.Service

@Service
class GetLabelsService(private val githubClientService: GithubClientService) {
    fun getLabelsForOrg(org: String): List<String> =
        githubClientService
            .fetchReposForOrg(org)
            .flatMapParallel { getLabelsForRepo(org, it.name) }

    fun getLabelsForRepo(org: String, repo: String) =
        githubClientService
            .fetchLabelsForRepo(org, repo)
            .labelNames()

    fun getIssuesForMarkedForContribution(org: String): List<IssueSummary> =
        githubClientService
            .fetchReposForOrg(org)
            .filter { it.has_issues }
            .flatMapParallel { repository -> githubClientService.fetchIssuesForRepo(repository, org) }
            .filter { it.isOpenForContribution }
            .map { it.toSummary() }

}