package dev.bogwalk.bootrack.backend.repository.notification

import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToSessionNotification
import dev.bogwalk.bootrack.backend.schema.entities.*
import dev.bogwalk.bootrack.backend.schema.tables.*
import dev.bogwalk.bootrack.model.Notification
import dev.bogwalk.bootrack.model.SessionNotification
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.mergeFrom
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.json.extract

/**
 * Represents all data access operations involving [Notification] & [SessionNotification] records using the DAO approach.
 */
internal class DAONotificationRepository : NotificationRepository {
    override suspend fun getNotificationsByReceiver(userId: Int): List<SessionNotification> = query {
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
    }

    override suspend fun addNotifications(notifications: List<Notification>): List<Notification> = query {
        notifications.map { notification ->
            NotificationEntity.new {
                notificationType = NotificationTypeEntity.findById(notification.notificationTypeId)
                    ?: error("Could not find an associated NotificationTypeDao for NotificationEntity creation")
                receiver = UserEntity.findById(notification.receiverId)
                    ?: error("Could not find an associated UserEntity for NotificationEntity creation")
                sender = UserEntity.findById(notification.receiverId)
                    ?: error("Could not find an associated UserEntity for NotificationEntity creation")
                issue = IssueEntity.findById(
                    CompositeID {
                        it[Issues.projectId] = notification.projectId
                        it[Issues.number] = notification.issueNumber
                    }
                )
                    ?: error("Could not find an associated IssueEntity for NotificationEntity creation")
            }
                .let(NotificationEntity::toNotification)
        }
    }

    override suspend fun editNotification(notification: SessionNotification): Boolean = query {
        val entity = SessionNotificationEntity.findByIdAndUpdate(notification.id) {
            it.isRead = notification.isRead
        }

        entity != null
    }

    override suspend fun syncNotifications(): Boolean = query {
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