package nl.rdh.github.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalExceptionHandlerIT {

    @Autowired
    private lateinit var rest: TestRestTemplate

    @Test
    fun illegalArgument_isHandledWithProblemDetails() {
        val resp: ResponseEntity<Map<String, Any>> = rest.exchange(
            "/test/illegal-arg",
            HttpMethod.GET,
            null,
            object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {}
        )
        assertThat(resp.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val body = resp.body!!
        assertThat(body["title"]).isEqualTo("Invalid request")
        assertThat(body["type"]).isEqualTo("https://example.com/problems/illegal-argument")
        assertThat(body["detail"]).isEqualTo("bad input")
        assertThat(body["status"]).isEqualTo(400)
    }

    @Test
    fun validationError_isHandledWithProblemDetailsAndErrors() {
        val payload = mapOf("name" to "", "age" to 0)
        val headers = org.springframework.http.HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(payload, headers)
        val resp: ResponseEntity<Map<String, Any>> = rest.exchange(
            "/test/validation",
            HttpMethod.POST,
            entity,
            object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {}
        )
        assertThat(resp.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        val body = resp.body!!
        assertThat(body["title"]).isEqualTo("Validation error")
        assertThat(body["type"]).isEqualTo("https://example.com/problems/validation-error")
        @Suppress("UNCHECKED_CAST")
        val errors = body["errors"] as Map<String, Any>
        assertThat(errors.keys).contains("name", "age")
    }

    @Test
    fun errorResponseException_isPropagated() {
        val resp: ResponseEntity<Map<String, Any>> = rest.exchange(
            "/test/error-response",
            HttpMethod.GET,
            null,
            object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {}
        )
        assertThat(resp.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        val body = resp.body!!
        assertThat(body["status"]).isEqualTo(404)
        assertThat(body["detail"]).isEqualTo("Not here")
    }

    @Test
    fun genericException_isHandledAsInternalServerError() {
        val resp: ResponseEntity<Map<String, Any>> = rest.exchange(
            "/test/generic",
            HttpMethod.GET,
            null,
            object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {}
        )
        assertThat(resp.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        val body = resp.body!!
        assertThat(body["title"]).isEqualTo("Internal Server Error")
        assertThat(body["type"]).isEqualTo("https://example.com/problems/internal-server-error")
        assertThat(body["status"]).isEqualTo(500)
    }
}
