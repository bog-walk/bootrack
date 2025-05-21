package dev.bogwalk.bootrack.backend.schema.tables

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

internal object NotificationTypes : IdTable<Int>("notification_types") {
    override val id = integer("id").entityId()
    val message = varchar("message", 265)

    override val primaryKey = PrimaryKey(id)
}

internal object Notifications : LongIdTable("notifications") {
    val notificationTypeId = reference("notification_type_id", NotificationTypes)

    val receiverId = reference("receiver_id", Users)
    val senderId = reference("sender_id", Users)

    val issueNumber = long("issue_number")
    val projectId = integer("project_id")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        foreignKey(
            issueNumber,
            projectId,
            target = Issues.primaryKey,
            onDelete = ReferenceOption.CASCADE,
        )
    }
}

internal object SessionNotifications : LongIdTable("session_notifications") {
    val notificationTypeId = reference("notification_type_id", NotificationTypes)

    val receiverId = reference("receiver_id", Users)
    val senderId = reference("sender_id", Users)

    val issueNumber = long("issue_number")
    val projectId = integer("project_id")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    val isRead = bool("is_read").defaultExpression(Op.FALSE)

    init {
        foreignKey(
            issueNumber,
            projectId,
            target = Issues.primaryKey,
            onDelete = ReferenceOption.CASCADE,
        )
    }
}