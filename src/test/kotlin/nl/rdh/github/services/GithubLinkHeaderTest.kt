package nl.rdh.github.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GithubLinkHeaderTest {

    @Test
    fun `extract last page from header`() {
        val headerValue =
            """<https://api.github.com/repositories/2090979/labels?per_page=2&page=2>; rel="next", <https://api.github.com/repositories/2090979/labels?per_page=2&page=35>; rel="last""""

        assertEquals(35, GithubLinkHeader(headerValue).lastPageNumber)
    }
}