package nl.rdh.github.client

import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Label
import nl.rdh.github.client.model.Repository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

@Component
class GithubClient(
    @Value("\${github.api.token:#{null}}") val githubApiToken: String,
    @Value("\${github.api.url:https://api.github.com}") val githubApiUrl: String) {


    private val apiClient: RestClient = RestClient.builder()
        .requestFactory(HttpComponentsClientHttpRequestFactory())
        .defaultHeader("AUTHORIZATION", "token $githubApiToken")
        .baseUrl(githubApiUrl)
        .build()

    fun getLabelsForRepo(org: String, repo: String): ResponseEntity<List<Label>> {
        return apiClient.get()
            .uri("/repos/{org}/{repo}/labels", org, repo)
            .retrieve()
            .toEntity<List<Label>>()
    }

    fun getAllReposForOrg(org: String): ResponseEntity<List<Repository>> {
        return apiClient.get()
            .uri("/orgs/{org}/repos", org)
            .retrieve()
            .toEntity<List<Repository>>()
    }

    fun getLabelsForUrl(labelsUrl: String): ResponseEntity<List<Label>> {
        return apiClient.get()
            .uri(labelsUrl.removePrefix(githubApiUrl).removeSuffix("{/name}"))
            .retrieve()
            .toEntity<List<Label>>()
    }

    fun getLabelsForUrlAndPage(labelsUrl: String, page: Int): ResponseEntity<List<Label>> {
        return apiClient.get()
            .uri(labelsUrl.removePrefix(githubApiUrl).removeSuffix("{/name}") + "?page={page}", page)
            .retrieve()
            .toEntity<List<Label>>()
    }


    fun getIssuesForRepo(org: String, repo: String): ResponseEntity<List<Issue>> {
        return apiClient.get()
            .uri("/repos/{org}/{repo}/issues", org, repo)
            .retrieve()
            .toEntity<List<Issue>>()
    }

    fun getIssuesForRepoForPage(org: String, repo: String, page:Int): ResponseEntity<List<Issue>> {
        return apiClient.get()
            .uri("/repos/{org}/{repo}/issues?page={page}", org, repo, page)
            .retrieve()
            .toEntity<List<Issue>>()
    }

}