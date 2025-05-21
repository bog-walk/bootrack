package dev.bogwalk.bootrack.routes

import dev.bogwalk.bootrack.model.Location
import io.ktor.resources.*

/** Resource for type-safe requests responding on the `/projects` path. */
@Resource(Routes.ALL_PROJECTS)
class Projects {

    /** Resource for type-safe requests with a path parameter responding on the `/projects/{projectId}` path. */
    @Resource(Routes.BY_PROJECT_ID)
    class Id(val parent: Projects = Projects(), val projectId: Int) {

        /** Resource for type-safe requests responding on the `/projects/{projectId}/issues` path. */
        @Resource(Routes.ALL_PROJECT_ISSUES)
        class Issues(
            val parent: Id,
            val limit: Int? = null,
            val offset: Int? = null,
            val searchText: String = "",
            val hideResolved: Boolean = false,
            val sortBy: String = "updated",
        ) {

            /** Resource for type-safe requests responding on the `/projects/{projectId}/issues/count` path. */
            @Resource(Routes.COUNT)
            class Count(val parent: Issues)

            /** Resource for type-safe requests responding on the `/projects/{projectId}/issues/rank` path. */
            @Resource(Routes.BY_RANK)
            class Rank(val parent: Issues, val orderBy: String)

            /** Resource for type-safe requests responding on the `/projects/{projectId}/issues/distance` path. */
            @Resource(Routes.BY_DISTANCE)
            class Distance(val parent: Issues, val targetLocation: Location, val maxDistance: Int)

            /** Resource for type-safe requests with a path parameter responding on the `/projects/{projectId}/issues/{issueNumber}` path. */
            @Resource(Routes.BY_ISSUE_ID)
            class Number(val parent: Issues, val issueNumber: Long, val userId: Int? = null, val toggle: String? = null) {

                /** Resource for type-safe requests responding on the `/projects/{projectId}/issues/{issueNumber}/comments` path. */
                @Resource(Routes.COMMENTS)
                class Comments(val parent: Number) {

                    /** Resource for type-safe requests with a path parameter responding on the `/projects/{projectId}/issues/{issueNumber}/comments/{commentId}` path. */
                    @Resource(Routes.BY_COMMENT_ID)
                    class CommentId(val parent: Comments, val commentId: Long)
                }
            }
        }
    }
}

/** Resource for type-safe requests responding on the `/users` path. */
@Resource(Routes.ALL_USERS)
class Users {

    /** Resource for type-safe requests with a path parameter responding on the `/users/{userId}` path. */
    @Resource(Routes.BY_USER_ID)
    class Id(val parent: Users = Users(), val userId: Int)
}

/** Resource for type-safe requests responding on the `/notifications` path. */
@Resource(Routes.ALL_NOTIFICATIONS)
class Notifications {

    /** Resource for type-safe requests with a path parameter responding on the `/notifications/{userId}` path. */
    @Resource(Routes.BY_USER_ID)
    class UserId(val parent: Notifications = Notifications(), val userId: Int)

    /** Resource for type-safe requests with a path parameter responding on the `/notifications/{notificationId}` path. */
    @Resource(Routes.BY_NOTIFICATION_ID)
    class NotificationId(val parent: Notifications = Notifications(), val notificationId: Long)

    /** Resource for type-safe requests with a path parameter responding on the `/notifications/sync` path. */
    @Resource(Routes.SYNC)
    class Sync(val parent: Notifications = Notifications())
}

/** Resource for type-safe requests responding on the `/shutdown` path. */
@Resource(Routes.SHUTDOWN)
class BackendShutdown
