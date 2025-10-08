package nl.rdh.github.api.v1

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.services.GetLabelsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(
    name = "GitHub repo contribution issues",
    description = "API for retrieving GitHub issues that are open for contribution"
)
class Controller(private val getLabelService: GetLabelsService) {

    @GetMapping("/labels/{org}/{repo}")
    @Operation(
        summary = "Get labels for a specific repository",
        description = "Retrieves all labels for a given organization and repository"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved labels",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = String::class))
                )]
            ),
            ApiResponse(responseCode = "404", description = "Repository not found", content = [Content()])
        ]
    )
    fun getLabelsForRepo(
        @Parameter(description = "GitHub organization name", required = true, example = "spring-projects")
        @PathVariable org: String,
        @Parameter(description = "GitHub repository name", required = true, example = "spring-boot")
        @PathVariable repo: String,
    ): List<String> = getLabelService.getLabelsForRepo(org, repo)

    @GetMapping("/labels/{org}")
    @Operation(
        summary = "Get all labels for an organization",
        description = "Retrieves all unique labels across all repositories in the given organization"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved labels",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = String::class))
                )]
            ),
            ApiResponse(responseCode = "404", description = "Organization not found", content = [Content()])
        ]
    )
    fun getAllLabelsForOrg(
        @Parameter(description = "GitHub organization name", required = true, example = "spring-projects")
        @PathVariable org: String,
    ) = getLabelService.getLabelsForOrg(org)

    @GetMapping("/contribution-issues/{org}")
    @Operation(
        summary = "Get contribution issues for an organization",
        description = "Retrieves all open issues marked as open for contribution across all repositories in the organization"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved contribution issues",
                content = [Content(
                    mediaType = "application/json",
                    array = ArraySchema(schema = Schema(implementation = IssueSummary::class))
                )]
            ),
            ApiResponse(responseCode = "404", description = "Organization not found", content = [Content()])
        ]
    )
    fun getAllIssuesOpenForContributionForOrg(
        @Parameter(description = "GitHub organization name", required = true, example = "spring-projects")
        @PathVariable org: String,
    ): List<IssueSummary> = getLabelService.getIssuesForMarkedForContribution(org)
}