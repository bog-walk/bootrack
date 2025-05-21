package dev.bogwalk.bootrack.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long,
    val issueNumber: Long,
    val projectId: Int,
    val authorId: Int,
    val content: String,
    val createdAt: Instant,
    val modifiedAt: Instant,
)
