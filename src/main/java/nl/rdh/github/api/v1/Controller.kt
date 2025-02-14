package nl.rdh.github.api.v1

import nl.rdh.github.api.v1.model.IssueSummary
import nl.rdh.github.services.GetLabelsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(private val getLabelService: GetLabelsService) {

    @GetMapping("/labels/{org}/{repo}")
    fun getLabelsForRepo(@PathVariable org: String, @PathVariable repo: String): MutableList<String>? {
        return getLabelService.getLabelsForRepo(org, repo)
    }

    @GetMapping("/labels/{org}")
    fun getAllLabelsForOrg(@PathVariable org: String): List<String> {
        return getLabelService.getLabelsForOrg(org)
    }

    @GetMapping("/contribution-issues/{org}")
    fun getAllIssuesOpenForContributionForOrg(@PathVariable org: String): MutableList<IssueSummary>? {
        return getLabelService.getIssuesForMarkedForContribution(org)
    }
}