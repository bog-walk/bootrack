package dev.bogwalk.bootrack.backend.repository.notification

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.backend.repository.utils.Converters.returnRowToNotification
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToSessionNotification
import dev.bogwalk.bootrack.backend.schema.tables.*
import dev.bogwalk.bootrack.model.Notification
import dev.bogwalk.bootrack.model.SessionNotification
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.json.extract
import org.jetbrains.exposed.v1.r2dbc.*

/**
 * Represents all server-side operations for defining and manipulating stored [Notification] & [SessionNotification] data.
 */
internal class NotificationRepository : BaseRepository {
    /**
     * Queries all stored [Notification] records that reference the user with the specified [userId].
     *
     * @return List of [SessionNotification] that only includes details relevant to the user of the active session.
     */
    suspend fun getNotificationsByReceiver(userId: Int): List<SessionNotification> = query {
        val mainQuery = Notifications
            .selectAll()
            .where { Notifications.receiverId eq userId }

        SessionNotifications.insert(
            selectQuery = mainQuery,
            columns = SessionNotifications.columns - SessionNotifications.isRead
        )

        val receiver = Users.alias("receiver")
        val sender = Users.alias("sender")

        val avatarIcon = sender[Users.settings].extract<Int>("avatarIcon").alias("icon")
        val avatarTint = sender[Users.settings].extract<Int>("avatarTint").alias("tint")

        SessionNotifications
            .leftJoin(NotificationTypes)
            .leftJoin(
                otherTable = receiver,
                onColumn = { SessionNotifications.receiverId },
                otherColumn = { receiver[Users.id] }
            )
            .leftJoin(
                otherTable = sender,
                onColumn = { SessionNotifications.senderId },
                otherColumn = { sender[Users.id] }
            )
            .join(
                otherTable = Issues,
                joinType = JoinType.LEFT,
                onColumn = SessionNotifications.projectId,
                otherColumn = Issues.projectId,
                additionalConstraint = { SessionNotifications.issueNumber eq Issues.number }
            )
            .select(
                SessionNotifications.id,
                SessionNotifications.receiverId,
                NotificationTypes.message,
                sender[Users.fullName],
                avatarIcon,
                avatarTint,
                Issues.code,
                SessionNotifications.createdAt,
                SessionNotifications.isRead
            )
            .orderBy(SessionNotifications.createdAt, SortOrder.DESC)
            .map { rowToSessionNotification(it, avatarIcon, avatarTint, sender) }
            .toList()
    }

    /**
     * Inserts a new batch of [SessionNotification] record.
     *
     * @return Whether the insert operation was successful.
     */
    suspend fun addNotifications(notifications: List<Notification>): List<Notification> = query {
        val dbGeneratedColumns = listOf(Notifications.id, Notifications.createdAt)

        notifications.map { notification ->
            val returnStmt = Notifications
                .insertReturning(
                    returning = dbGeneratedColumns
                ) {
                    it[notificationTypeId] = notification.notificationTypeId
                    it[receiverId] = notification.receiverId
                    it[senderId] = notification.senderId
                    it[issueNumber] = notification.issueNumber
                    it[projectId] = notification.projectId
                }

            returnStmt
                .singleOrNull()
                ?.let { returnRowToNotification(it, notification) }
                ?: error("Database-generated value not returned on issue update")
        }
    }

    /**
     * Updates a stored [SessionNotification] record to have a different `isRead` value.
     *
     * @return Whether the update operation was successful.
     */
    suspend fun editNotification(notification: SessionNotification): Boolean = query {
        SessionNotifications
            .update(
                where = { SessionNotifications.id eq notification.id }
            ) {
                it[isRead] = notification.isRead
            } == 1
    }

    /**
     * Synchronizes all [Notification] records to match those in the active session.
     *
     * If a notification has been read during the current session, it will be permanently deleted from the synchronized table.
     *
     * @return Whether the merge operation was successful.
     */
    suspend fun syncNotifications(): Boolean = query {
        Notifications
            .mergeFrom(SessionNotifications) {
                whenNotMatchedInsert {
                    it[Notifications.notificationTypeId] = SessionNotifications.notificationTypeId
                    it[Notifications.receiverId] = SessionNotifications.receiverId
                    it[Notifications.senderId] = SessionNotifications.senderId
                    it[Notifications.issueNumber] = SessionNotifications.issueNumber
                    it[Notifications.projectId] = SessionNotifications.projectId
                    it[Notifications.createdAt] = SessionNotifications.createdAt
                }

                whenMatchedDoNothing(and = SessionNotifications.isRead eq false)

                whenMatchedDelete()
            }

        true
    }
}
