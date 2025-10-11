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
    @param:Value("\${github.api.url:https://api.github.com}") private val githubApiUrl: String,
) {

    private val apiClient: RestClient = buildRestClient()

    fun getLabelsForRepo(org: String, repo: String, page: Int? = null): ResponseEntity<List<Label>> =
        apiClient.get()
            .uri("/repos/{org}/{repo}/labels" + getPageParam(page), org, repo)
            .retrieve()
            .toEntity()

    fun getAllReposForOrg(org: String, page: Int? = null): ResponseEntity<List<Repository>> =
        apiClient.get()
            .uri("/orgs/{org}/repos" + getPageParam(page), org)
            .retrieve()
            .toEntity()

    fun getIssuesForRepo(org: String, repo: String, page: Int? = null): ResponseEntity<List<Issue>> =
        apiClient.get()
            .uri("/repos/{org}/{repo}/issues" + getPageParam(page), org, repo)
            .retrieve()
            .toEntity()

    private fun getPageParam(page: Int?) = page?.let { "?page=${page}" } ?: ""

    private fun buildRestClient() = RestClient.builder()
        .requestFactory(HttpComponentsClientHttpRequestFactory())
        .baseUrl(githubApiUrl)
        .also { builder ->
            if (githubApiToken.isNullOrBlank().not())
                builder.defaultHeader("AUTHORIZATION", "token $githubApiToken")
        }.build()
}