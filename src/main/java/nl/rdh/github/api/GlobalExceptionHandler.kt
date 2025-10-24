package nl.rdh.github.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ProblemDetail> {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")
        pd.type = URI.create("https://example.com/problems/illegal-argument")
        pd.title = "Invalid request"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed")
        pd.type = URI.create("https://example.com/problems/validation-error")
        pd.title = "Validation error"
        pd.setProperty("errors", ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid") })
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd)
    }

    @ExceptionHandler(ErrorResponseException::class)
    fun handleErrorResponse(ex: ErrorResponseException): ResponseEntity<ProblemDetail> {
        if (log.isWarnEnabled) log.warn("Client error: {}", ex.message)
        return ResponseEntity.status(ex.statusCode).body(ex.body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ProblemDetail> {
        log.error("Unhandled error", ex)
        val pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
        pd.type = URI.create("https://example.com/problems/internal-server-error")
        pd.title = "Internal Server Error"
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd)
    }
}