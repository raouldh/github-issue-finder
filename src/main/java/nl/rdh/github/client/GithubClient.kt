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
    @param:Value("\${github.api.token:}") private val githubApiToken: String?,
    @param:Value("\${github.api.url:https://api.github.com}") private val githubApiUrl: String
) {

    private val apiClient: RestClient = buildRestClient()

    private fun buildRestClient(): RestClient {
        val restClientBuilder = RestClient.builder()
            .requestFactory(HttpComponentsClientHttpRequestFactory())
        
        githubApiToken?.takeIf { it.isNotBlank() }?.let { token ->
            restClientBuilder.defaultHeader("AUTHORIZATION", "token $token")
        }
        
        return restClientBuilder
            .baseUrl(githubApiUrl)
            .build()
    }

    fun getLabelsForRepo(org: String, repo: String): ResponseEntity<List<Label>> =
        apiClient.get()
            .uri("/repos/{org}/{repo}/labels", org, repo)
            .retrieve()
            .toEntity()

    fun getAllReposForOrg(org: String): ResponseEntity<List<Repository>> =
        apiClient.get()
            .uri("/orgs/{org}/repos", org)
            .retrieve()
            .toEntity()

    fun getLabelsForUrl(labelsUrl: String): ResponseEntity<List<Label>> =
        apiClient.get()
            .uri(labelsUrl.removePrefix(githubApiUrl).removeSuffix("{/name}"))
            .retrieve()
            .toEntity()

    fun getLabelsForUrlAndPage(labelsUrl: String, page: Int): ResponseEntity<List<Label>> =
        apiClient.get()
            .uri("${labelsUrl.removePrefix(githubApiUrl).removeSuffix("{/name}")}?page={page}", page)
            .retrieve()
            .toEntity()

    fun getIssuesForRepo(org: String, repo: String): ResponseEntity<List<Issue>> =
        apiClient.get()
            .uri("/repos/{org}/{repo}/issues", org, repo)
            .retrieve()
            .toEntity()

    fun getIssuesForRepoForPage(org: String, repo: String, page: Int): ResponseEntity<List<Issue>> =
        apiClient.get()
            .uri("/repos/{org}/{repo}/issues?page={page}", org, repo, page)
            .retrieve()
            .toEntity()
}