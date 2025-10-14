package nl.rdh.github.services

import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder.fromUriString

data class GithubLinkHeader(private val headerValue: String?) {
    val lastPageNumber
        get() = headerValue
            ?.let {
                lastPageUrlRegex
                    .find(it)
                    ?.groupValues
                    ?.getOrNull(1)
            }
            ?.let {
                fromUriString(it)
                    .build()
                    .queryParams
                    .getFirst(PAGE_PARAM)
                    ?.toIntOrNull()
            }
            ?: 1

    companion object {
        private const val LINK_HEADER_NAME = "Link"
        private const val PAGE_PARAM = "page"
        private val lastPageUrlRegex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)

        fun from(headers: MultiValueMap<String, String>): GithubLinkHeader {
            return GithubLinkHeader(headers[LINK_HEADER_NAME].orEmpty<String>().firstOrNull())
        }
    }
}