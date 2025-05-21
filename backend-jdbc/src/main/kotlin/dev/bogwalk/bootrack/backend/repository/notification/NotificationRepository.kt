package dev.bogwalk.bootrack.backend.repository.notification

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.model.Notification
import dev.bogwalk.bootrack.model.SessionNotification

/**
 * Base representation of all server-side operations for defining and manipulating stored [Notification] & [SessionNotification] data.
 */
internal interface NotificationRepository : BaseRepository {
    /**
     * Queries all stored [Notification] records that reference the user with the specified [userId].
     *
     * @return List of [SessionNotification] that only includes details relevant to the user of the active session.
     */
    suspend fun getNotificationsByReceiver(userId: Int): List<SessionNotification>

    /**
     * Inserts a new batch of [SessionNotification] record.
     *
     * @return Whether the insert operation was successful.
     */
    suspend fun addNotifications(notifications: List<Notification>): List<Notification>

    /**
     * Updates a stored [SessionNotification] record to have a different `isRead` value.
     *
     * @return Whether the update operation was successful.
     */
    suspend fun editNotification(notification: SessionNotification): Boolean

    /**
     * Synchronizes all [Notification] records to match those in the active session.
     *
     * If a notification has been read during the current session, it will be permanently deleted from the synchronized table.
     *
     * @return Whether the merge operation was successful.
     */
    suspend fun syncNotifications(): Boolean
}