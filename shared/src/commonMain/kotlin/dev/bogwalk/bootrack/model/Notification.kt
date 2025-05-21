package dev.bogwalk.bootrack.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SessionNotification(
    val id: Long,
    val receiverId: Int,
    val message: String,
    val senderName: String,
    val senderAvatar: Int,
    val senderAvatarTint: Int,
    val issueCode: String,
    val createdAt: Instant,
    val isRead: Boolean,
)

@Serializable
data class Notification(
    val id: Long,
    val notificationTypeId: Int,
    val receiverId: Int,
    val senderId: Int,
    val issueNumber: Long,
    val projectId: Int,
    val createdAt: Instant,
)

sealed class NotificationType(
    val id: Int,
    val message: String,
) {
    object UpdatedIssue : NotificationType(1, " updated details in ")
    object ClosedIssue : NotificationType(2, " closed ")
    object DeletedIssue : NotificationType(3, " deleted ")
    object CommentedOnIssue : NotificationType(4, " left a comment in ")
    object Mentioned : NotificationType(5, " mentioned you in ")
}
