package nl.rdh.github.services

import nl.rdh.github.flatMapParallel
import nl.rdh.github.mapParallel
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap

const val x = """
<https://api.github.com/repositories/2090979/labels?per_page=2&page=2>; rel="next", <https://api.github.com/repositories/2090979/labels?per_page=2&page=35>; rel="last"
"""

data class GithubResponse(
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


fun ResponseEntity<*>.toGithubResponse() = GithubResponse(this)