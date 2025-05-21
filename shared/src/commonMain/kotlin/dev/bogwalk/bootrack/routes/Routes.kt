package dev.bogwalk.bootrack.routes

/** All string paths for type-safe routing and requests. */
internal object Routes {
    const val ALL_USERS = "/users"
    const val BY_USER_ID = "{userId}"

    const val ALL_PROJECTS = "/projects"
    const val BY_PROJECT_ID = "{projectId}"

    const val ALL_PROJECT_ISSUES = "issues"
    const val COUNT = "count"
    const val BY_RANK = "rank"
    const val BY_DISTANCE = "distance"
    const val BY_ISSUE_ID = "{issueNumber}"
    const val COMMENTS = "comments"
    const val BY_COMMENT_ID = "{commentId}"

    const val ALL_NOTIFICATIONS = "/notifications"
    const val BY_NOTIFICATION_ID = "{notificationId}"
    const val SYNC = "sync"

    const val SHUTDOWN = "/shutdown"
}
