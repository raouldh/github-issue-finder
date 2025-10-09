package nl.rdh.github.services

import org.springframework.web.util.UriComponentsBuilder.fromUriString

data class GithubLinkHeader(private val header: String?) {
    val lastPageNumber
        get() = header
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
        const val PAGE_PARAM = "page"
        val lastPageUrlRegex = """<([^>]+)>;\s*rel="last"""".toRegex(RegexOption.IGNORE_CASE)
    }
}