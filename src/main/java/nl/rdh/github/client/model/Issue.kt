package nl.rdh.github.client.model

data class Issue(
    val active_lock_reason: Any?,
    val assignee: Any?,
    val assignees: List<Any?>?,
    val author_association: String?,
    val body: String?,
    val closed_at: Any?,
    val closed_by: Any?,
    val comments: Int?,
    val comments_url: String?,
    val created_at: String,
    val draft: Boolean?,
    val events_url: String?,
    val html_url: String,
    val id: Long,
    val labels: List<Label>?,
    val labels_url: String?,
    val locked: Boolean?,
    val milestone: Milestone?,
    val node_id: String,
    val number: Int?,
    val performed_via_github_app: Any?,
    val pull_request: PullRequest?,
    val reactions: Reactions?,
    val repository_url: String?,
    val state: String?,
    val state_reason: Any?,
    val timeline_url: String?,
    val title: String,
    val updated_at: String?,
    val url: String,
    val user: User?,
) {
    val isOpenForContribution = hasAnyLabelIn(OPEN_FOR_CONTRIBUTION_LABELS)

    val labelNames = this.labels.orEmpty().map { it.name }

    fun hasAnyLabelIn(labels: Collection<String>) = this.labels
        .orEmpty()
        .any { label -> label.name in labels }

    private companion object {
        // This list is compiled from all unique labels for all spring-cloud and spring-projects repo's
        val OPEN_FOR_CONTRIBUTION_LABELS = setOf(
            "ideal-for-contribution",
            "ideal-for-user-contribution",
            "meta: contributions welcome",
            "meta: first timers only",
            "status: ideal-for-contribution",
            "status: need-help-to-reproduce",
            "status: first-timers-only",
            "status/first-timers-only",
            "community contribution",
            "contribution welcome",
            "help wanted",
            "help-wanted",
            "first-timers-only",
            "good first issue",
            "type/help-needed"
        )
    }
}