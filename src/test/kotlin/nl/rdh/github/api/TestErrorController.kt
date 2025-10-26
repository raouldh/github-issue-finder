package nl.rdh.github.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/test")
class TestErrorController {

    @GetMapping("/illegal-arg")
    fun illegalArg(): String {
        throw IllegalArgumentException("bad input")
    }

    data class ValidationRequest(
        @field:NotBlank
        val name: String?,
        @field:Min(1)
        val age: Int?
    )

    @PostMapping("/validation")
    fun validation(@RequestBody @Valid body: ValidationRequest): String {
        return "ok"
    }

    @GetMapping("/error-response")
    fun errorResponse(): String {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Not here")
        throw ErrorResponseException(HttpStatus.NOT_FOUND, pd, null)
    }

    @GetMapping("/generic")
    fun generic(): String {
        throw RuntimeException("boom")
    }
}
