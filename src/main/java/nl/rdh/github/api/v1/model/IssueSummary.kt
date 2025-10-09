package nl.rdh.github.api.v1.model

import io.swagger.v3.oas.annotations.media.Schema
import nl.rdh.github.client.model.Issue

@Schema(description = "Summary of a GitHub issue marked for contribution")
data class IssueSummary(
    @get:Schema(
        description = "API URL of the issue",
        example = "https://api.github.com/repos/spring-projects/spring-boot/issues/12345"
    )
    val url: String,

    @get:Schema(
        description = "HTML URL of the issue (web page)",
        example = "https://github.com/spring-projects/spring-boot/issues/12345"
    )
    val htmlUrl: String,

    @get:Schema(
        description = "Title of the issue",
        example = "Add support for custom configuration properties"
    )
    val title: String,

    @get:Schema(
        description = "Current state of the issue",
        example = "open",
        allowableValues = ["open", "closed"]
    )
    val state: String,

    @get:Schema(
        description = "Number of comments on the issue",
        example = "5",
        minimum = "0"
    )
    val comments: Int,

    @get:Schema(
        description = "List of label names associated with the issue",
        example = "[\"good first issue\", \"help wanted\", \"enhancement\"]"
    )
    val labels: List<String>,
)

fun Issue.toSummary() = IssueSummary(
    url = url,
    htmlUrl = html_url,
    title = title,
    state = state.orEmpty(),
    comments = comments ?: 0,
    labels = labelNames
)