package nl.rdh.github.services

import nl.rdh.github.bodyAsList
import org.springframework.http.ResponseEntity

data class GithubPagedRequest<T>(
    val initialCall: () -> ResponseEntity<List<T>>,
    val callForPage: (Int) -> ResponseEntity<List<T>>,
) {
    fun execute() = initialCall()
        .let {
            it.bodyAsList() + it
                .toGithubResponse()
                .flatMapAdditionalPages { pageNumber -> callForPage(pageNumber).bodyAsList() }
        }
}