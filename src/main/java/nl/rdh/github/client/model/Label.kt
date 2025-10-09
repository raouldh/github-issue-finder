package nl.rdh.github.client.model

data class Label(
    val id: Long, val node_id: String?, val url: String, val name: String, val color: String,
    val default: Boolean?, val description: String?,
)

fun List<Label>.labelNames(): List<String> = this
    .map { it.name }
    .distinct()
    .sorted()