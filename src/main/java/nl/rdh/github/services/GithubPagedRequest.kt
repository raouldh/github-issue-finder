package nl.rdh.github.services

import nl.rdh.github.bodyAsList
import org.springframework.http.ResponseEntity

data class GithubPagedRequest<T>(
    val initialCall: () -> ResponseEntity<List<T>>,
    val callForPage: (Int) -> ResponseEntity<List<T>>,
) {
    fun execute(): List<T> {
        val firstResponse = initialCall()

        val otherPageResponses = firstResponse
            .toGithubResponse()
            .mapAdditionalPages { callForPage(it) }

        return firstResponse.bodyAsList() + otherPageResponses.flatMap { it.bodyAsList() }
    }
}