package nl.rdh.github.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "github.api")
data class GithubProperties(
    val token: String? = null,
    @field:NotBlank val url: String = "https://api.github.com",
)