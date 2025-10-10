package nl.rdh.github.services

import nl.rdh.github.bodyAsList
import nl.rdh.github.client.GithubClient
import nl.rdh.github.client.model.Repository
import nl.rdh.github.flatMapParallel
import nl.rdh.github.mapParallel
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap

@Service
class GithubClientService(private val githubClient: GithubClient) {

    fun fetchReposForOrg(org: String) = GithubPagedRequest(
        { githubClient.getAllReposForOrg(org) },
        { githubClient.getAllReposForOrg(org, it) }
    ).execute()

    fun fetchLabelsForRepo(org: String, repository: String) = GithubPagedRequest(
        { githubClient.getLabelsForRepo(org, repository) },
        { githubClient.getLabelsForRepo(org, repository, it) }
    ).execute()


    fun fetchIssuesForRepo(repository: Repository, org: String) = GithubPagedRequest(
        { githubClient.getIssuesForRepo(org, repository.name) },
        { githubClient.getIssuesForRepo(org, repository.name, it) }
    ).execute()

    private data class GithubPagedRequest<T>(
        val initialCall: () -> ResponseEntity<List<T>>,
        val callForPage: (Int) -> ResponseEntity<List<T>>,
    ) {
        fun execute() = initialCall()
            .let {
                it.bodyAsList() + it
                    .toGithubResponse()
                    .flatMapAdditionalPages { pageNumber -> callForPage(pageNumber).bodyAsList() }
            }

        private fun ResponseEntity<*>.toGithubResponse() = GithubResponse(this)
    }

    private data class GithubResponse(
        val response: ResponseEntity<*>,
    ) {
        private val headers: MultiValueMap<String, String> = response.headers
        val lastPageNumber
            get() = GithubLinkHeader(headers[LINK_HEADER].orEmpty<String>().firstOrNull()).lastPageNumber

        fun <T> mapAdditionalPages(block: (Int) -> T): List<T> = (2..lastPageNumber)
            .toList()
            .mapParallel { page -> block(page) }

        fun <T> flatMapAdditionalPages(block: (Int) -> List<T>): List<T> = (2..lastPageNumber)
            .toList()
            .flatMapParallel { page -> block(page) }

        companion object {
            const val LINK_HEADER = "Link"
        }
    }
}