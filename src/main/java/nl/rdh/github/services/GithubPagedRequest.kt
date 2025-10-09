package nl.rdh.github.services

import org.springframework.http.ResponseEntity

data class GithubPagedRequest<T>(
    val initialCall: () -> ResponseEntity<T>,
    val callForPage: (Int) -> ResponseEntity<T>,
) {
    fun getResponses(): List<ResponseEntity<T>> {
        val firstResponse = initialCall()

        val otherPageResponses = firstResponse
            .toGithubResponse()
            .mapAdditionalPages { callForPage(it) }

        return listOf(firstResponse) + otherPageResponses
    }
}