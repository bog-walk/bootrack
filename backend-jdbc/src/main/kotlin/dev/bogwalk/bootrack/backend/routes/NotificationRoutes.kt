package dev.bogwalk.bootrack.backend.routes

import dev.bogwalk.bootrack.backend.repository.notification.NotificationRepository
import dev.bogwalk.bootrack.model.Notification
import dev.bogwalk.bootrack.model.SessionNotification
import dev.bogwalk.bootrack.routes.Notifications
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route

/** Route handler for the `Notifications` resource class. */
internal fun Route.notificationRoutes(repository: NotificationRepository) {
    post<Notifications> {
        val notifications = call.receive<List<Notification>>()
        try {
            val batchCreated = repository.addNotifications(notifications)
            call.respond(HttpStatusCode.Created, batchCreated)
        } catch (cause: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    get<Notifications.UserId> {
        val notifications = repository.getNotificationsByReceiver(it.userId)
        call.respond(notifications)
    }

    put<Notifications.NotificationId> {
        val updated = call.receive<SessionNotification>()
        if (repository.editNotification(updated)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    get<Notifications.Sync> {
        val notifications = repository.syncNotifications()
        call.respond(notifications)
    }
}
