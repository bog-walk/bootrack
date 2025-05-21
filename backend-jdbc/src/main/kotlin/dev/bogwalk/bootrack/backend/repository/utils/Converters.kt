package dev.bogwalk.bootrack.backend.repository.utils

import dev.bogwalk.bootrack.backend.repository.custom.With
import dev.bogwalk.bootrack.backend.schema.tables.*
import dev.bogwalk.bootrack.model.*
import org.jetbrains.exposed.v1.core.*

internal object Converters {
    fun rowToUser(result: ResultRow): User = User(
        id = result[Users.id].value,
        fullName = result[Users.fullName],
        username = result[Users.username],
        settings = result[Users.settings],
    )

    fun rowToProject(result: ResultRow): Project = Project(
        id = result[Projects.id].value,
        name = result[Projects.name],
        code = result[Projects.code],
    )

    fun rowToIssueSummarized(
        result: ResultRow,
        countExpression: ExpressionWithColumnTypeAlias<Long>,
        locationExpression: Expression<Location?>? = null,
        altSource: With? = null,
    ): IssueSummarized = IssueSummarized(
        issue = rowToIssue(result, false, locationExpression, altSource, null),
        commentCount = altSource?.let { result[it[countExpression]] }?.toInt() ?: result[countExpression].toInt()
    )

    fun rowsToIssueDetailed(
        result: List<ResultRow>,
        locationExpression: Expression<Location?>,
        qAlias: QueryAlias? = null,
    ): IssueDetailed = IssueDetailed(
        issue = rowToIssue(result.first(), true, locationExpression, null, qAlias),
        comments = result.mapNotNull(::rowToComment),
    )

    fun returnRowToIssue(
        result: ResultRow,
        original: Issue,
    ): Issue {
        val dbNumber = result.getOrNull(Issues.number)?.value ?: original.number
        val dbCode = result.getOrNull(Issues.code) ?: original.code
        val dbWatchers = result.getOrNull(Issues.watchers) ?: original.watchers
        val dbUpvotes = result.getOrNull(Issues.upvotes) ?: original.upvotes
        val dbCreatedAt = result.getOrNull(Issues.createdAt) ?: original.createdAt
        val dbModifiedAt = result.getOrNull(Issues.modifiedAt) ?: original.modifiedAt

        return original.copy(
            number = dbNumber,
            code = dbCode,
            watchers = dbWatchers,
            upvotes = dbUpvotes,
            createdAt = dbCreatedAt,
            modifiedAt = dbModifiedAt
        )
    }

    fun returnRowToComment(
        result: ResultRow,
        original: Comment,
    ): Comment {
        val dbId = result.getOrNull(Comments.id)?.value ?: original.id
        val dbCreatedAt = result.getOrNull(Comments.createdAt) ?: original.createdAt
        val dbModifiedAt = result.getOrNull(Comments.modifiedAt) ?: original.modifiedAt

        return original.copy(
            id = dbId,
            createdAt = dbCreatedAt,
            modifiedAt = dbModifiedAt
        )
    }

    fun rowToSessionNotification(
        result: ResultRow,
        userAvatarExpression: Expression<Int>,
        userAvatarTintExpression: Expression<Int>,
        tAlias: Alias<*>? = null,
    ): SessionNotification = SessionNotification(
        id = result[SessionNotifications.id].value,
        receiverId = result[SessionNotifications.receiverId].value,
        message = result[NotificationTypes.message],
        senderName = result[Users.fullName.unaliased(tAlias = tAlias)],
        senderAvatar = result[userAvatarExpression],
        senderAvatarTint = result[userAvatarTintExpression],
        issueCode = result[Issues.code],
        createdAt = result[SessionNotifications.createdAt],
        isRead = result[SessionNotifications.isRead],
    )

    fun returnRowToNotification(
        result: ResultRow,
        original: Notification,
    ): Notification {
        val dbId = result.getOrNull(Notifications.id)?.value ?: original.id
        val dbCreatedAt = result.getOrNull(Notifications.createdAt) ?: original.createdAt

        return original.copy(
            id = dbId,
            createdAt = dbCreatedAt,
        )
    }

    private fun rowToIssue(
        result: ResultRow,
        withDescription: Boolean,
        locationExpression: Expression<Location?>?,
        altSource: With?,
        qAlias: QueryAlias?,
    ): Issue = Issue(
        number = result[Issues.number.unaliased(qAlias, altSource)].value,
        projectId = result[Issues.projectId.unaliased(qAlias, altSource)].value,
        code = result[Issues.code.unaliased(qAlias, altSource)],
        authorId = result[Issues.authorId.unaliased(qAlias, altSource)].value,
        assigneeId = result[Issues.assigneeId.unaliased(qAlias, altSource)]?.value,
        title = result[Issues.title.unaliased(qAlias, altSource)],
        description = if (withDescription) result[Issues.description.unaliased(qAlias, altSource)] else "",
        priority = result[Issues.priority.unaliased(qAlias, altSource)],
        state = result[Issues.state.unaliased(qAlias, altSource)],
        location = if (locationExpression != null) result[locationExpression] else null,
        watchers = result[Issues.watchers.unaliased(qAlias, altSource)],
        upvotes = result[Issues.upvotes.unaliased(qAlias, altSource)],
        createdAt = result[Issues.createdAt.unaliased(qAlias, altSource)],
        modifiedAt = result[Issues.modifiedAt.unaliased(qAlias, altSource)],
    )

    private fun <T> Column<T>.unaliased(
        qAlias: QueryAlias? = null,
        altSource: With? = null,
        tAlias: Alias<*>? = null,
    ): Column<T> = qAlias?.let { it[this] }
        ?: altSource?.let { it[this] }
        ?: tAlias?.let { it[this] }
        ?: this

    private fun rowToComment(result: ResultRow): Comment? {
        if (result[Comments.id] == null) return null

        return Comment(
            id = result[Comments.id].value,
            issueNumber = result[Comments.issueNumber],
            projectId = result[Comments.projectId],
            authorId = result[Comments.authorId].value,
            content = result[Comments.content],
            createdAt = result[Comments.createdAt],
            modifiedAt = result[Comments.modifiedAt],
        )
    }
}