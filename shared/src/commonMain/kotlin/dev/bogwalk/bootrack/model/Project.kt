package dev.bogwalk.bootrack.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val name: String,
    val code: String
)
