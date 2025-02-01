package nl.rdh.github.api.v1

import nl.rdh.github.model.Issue
import nl.rdh.github.model.Label
import nl.rdh.github.model.Repository
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

private const val GITHUB_API = "https://api.github.com"
private const val GITHUB_TOKEN = "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"

@Component
class GithubClient {
    private val apiClient: RestClient = RestClient.builder()
        .requestFactory(HttpComponentsClientHttpRequestFactory())
        .defaultHeader("AUTHORIZATION", "token $GITHUB_TOKEN")
        .baseUrl(GITHUB_API)
        .build()

    fun getLabelsForRepo(owner: String, repo: String): ResponseEntity<List<Label>> {
        return apiClient.get()
            .uri("/repos/{owner}/{repo}/labels", owner, repo)
            .retrieve()
            .toEntity<List<Label>>()
    }

    fun getAllReposForOrg(org: String): ResponseEntity<List<Repository>> {
        return apiClient.get()
            .uri("/orgs/{org}/repos", org)
            .retrieve()
            .toEntity<List<Repository>>()
    }

    fun getLabelsUrl(labelsUrl: String): ResponseEntity<List<Label>> {
        return apiClient.get()
            .uri(labelsUrl.removePrefix(GITHUB_API).removeSuffix("{/name}"))
            .retrieve()
            .toEntity<List<Label>>()
    }

    fun getIssuesForRepo(owner: String, repo: String): ResponseEntity<List<Issue>> {
        return apiClient.get()
            .uri("/repos/{owner}/{repo}/issues", owner, repo)
            .retrieve()
            .toEntity<List<Issue>>()
    }
    fun getIssuesForRepoForPage(owner: String, repo: String, page:String): ResponseEntity<List<Issue>> {
        return apiClient.get()
            .uri("/repos/{owner}/{repo}/issues?page={page}", owner, repo, page)
            .retrieve()
            .toEntity<List<Issue>>()
    }

}