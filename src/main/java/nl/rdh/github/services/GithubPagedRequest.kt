package nl.rdh.github.services

import nl.rdh.github.bodyAsList
import org.springframework.http.ResponseEntity

data class GithubPagedRequest<T>(
    val initialCall: () -> ResponseEntity<List<T>>,
    val callForPage: (Int) -> ResponseEntity<List<T>>,
) {
    fun execute(): List<T> {
        val firstResponse = initialCall()

        val otherPageData = firstResponse
            .toGithubResponse()
            .flatMapAdditionalPages { callForPage(it).bodyAsList() }

        return firstResponse.bodyAsList() + otherPageData
    }
}