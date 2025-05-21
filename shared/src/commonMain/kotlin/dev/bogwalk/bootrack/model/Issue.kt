package dev.bogwalk.bootrack.model

import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val number: Long,
    val projectId: Int,
    val code: String,
    val authorId: Int,
    val assigneeId: Int?,
    val title: String,
    val description: String,
    val priority: IssuePriority,
    val state: IssueState,
    val location: Location?,
    val watchers: List<Int>,
    val upvotes: List<Int>,
    val createdAt: Instant,
    val modifiedAt: Instant,
)

@Serializable
data class IssueSummarized(
    val issue: Issue,
    val commentCount: Int,
)

@Serializable
data class IssueDetailed(
    val issue: Issue,
    val comments: List<Comment>,
)

fun Instant.format(pattern: String): String {
    val customFormat = UserDateFormat.entries
        .find { it.pattern == pattern }
        ?: error("Invalid date time format string found: $pattern")

    return this.format(customFormat.format)
}

enum class IssuePriority(val label: String) {
    NORMAL("Normal"),
    MINOR("Minor"),
    MAJOR("Major")
}

enum class IssueState(val label: String) {
    SUBMITTED("Submitted"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed")
}
