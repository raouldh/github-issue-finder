package nl.rdh.github.services

import nl.rdh.github.flatMapParallel
import nl.rdh.github.mapParallel
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap

data class GithubResponse(
    val response: ResponseEntity<*>,
) {
    private val headers: MultiValueMap<String, String> = response.headers
    private val linkHeaderValues: List<String?> = headers[LINK_HEADER].orEmpty()

    val lastPageNumber = linkHeaderValues
        .firstOrNull()
        ?.let { regexExtractLastPageUrl(it) }
        ?.substringAfter(PAGE_PARAM)
        ?.toIntOrNull()
        ?: 1

    private fun regexExtractLastPageUrl(linkHeader: String) = lastPageUrlRegex
        .find(linkHeader)
        ?.groupValues
        ?.getOrNull(1)

    fun <T> mapAdditionalPages(block: (Int) -> T): List<T> = (2..lastPageNumber)
        .toList()
        .mapParallel { page -> block(page) }

    fun <T> flatMapAdditionalPages(block: (Int) -> List<T>): List<T> = (2..lastPageNumber)
        .toList()
        .flatMapParallel { page -> block(page) }

    companion object {
        const val LINK_HEADER = "Link"
        const val PAGE_PARAM = "page="
        val lastPageUrlRegex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
    }
}


fun ResponseEntity<*>.toGithubResponse() = GithubResponse(this)