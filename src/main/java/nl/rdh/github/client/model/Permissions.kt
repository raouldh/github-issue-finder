package nl.rdh.github.client.model

data class Permissions(
    val admin: Boolean,
    val maintain: Boolean,
    val pull: Boolean,
    val push: Boolean,
    val triage: Boolean
)