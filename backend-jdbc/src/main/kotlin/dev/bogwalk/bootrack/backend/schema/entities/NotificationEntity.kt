package dev.bogwalk.bootrack.backend.schema.entities

import dev.bogwalk.bootrack.backend.schema.tables.NotificationTypes
import dev.bogwalk.bootrack.backend.schema.tables.Notifications
import dev.bogwalk.bootrack.backend.schema.tables.SessionNotifications
import dev.bogwalk.bootrack.model.Notification
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

internal class NotificationTypeEntity(id: EntityID<Int>) : IntEntity(id) {
    var message by NotificationTypes.message

    companion object : IntEntityClass<NotificationTypeEntity>(NotificationTypes)
}

internal class NotificationEntity(id: EntityID<Long>) : LongEntity(id) {
    var notificationType by NotificationTypeEntity referencedOn Notifications.notificationTypeId

    var receiver by UserEntity referencedOn Notifications.receiverId
    var sender by UserEntity referencedOn Notifications.senderId

    var issue by IssueEntity referencedOn Notifications

    var createdAt by Notifications.createdAt

    companion object : LongEntityClass<NotificationEntity>(Notifications)

    fun toNotification(): Notification = Notification(
        id = id.value,
        notificationTypeId = notificationType.id.value,
        receiverId = receiver.id.value,
        senderId = sender.id.value,
        issueNumber = issue.number,
        projectId = issue.projectId,
        createdAt = createdAt,
    )
}

internal class SessionNotificationEntity(id: EntityID<Long>) : LongEntity(id) {
    var notificationType by NotificationTypeEntity referencedOn SessionNotifications.notificationTypeId

    var receiver by UserEntity referencedOn SessionNotifications.receiverId
    var sender by UserEntity referencedOn SessionNotifications.senderId

    var issue by IssueEntity referencedOn SessionNotifications

    var createdAt by SessionNotifications.createdAt
    var isRead by SessionNotifications.isRead

    companion object : LongEntityClass<SessionNotificationEntity>(SessionNotifications)
}
