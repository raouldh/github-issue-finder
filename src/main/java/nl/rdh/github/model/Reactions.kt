package nl.rdh.github.model

data class Reactions(
    val `+1`: Int,
    val `-1`: Int,
    val confused: Int,
    val eyes: Int,
    val heart: Int,
    val hooray: Int,
    val laugh: Int,
    val rocket: Int,
    val total_count: Int,
    val url: String
)