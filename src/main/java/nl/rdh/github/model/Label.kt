package nl.rdh.github.model

data class Label(
    val id: Long, val node_id: String?, val url: String, val name: String, val color: String,
    val default: Boolean?, val description: String?
)