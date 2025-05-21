package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.repository.comment.CommentRepository
import dev.bogwalk.bootrack.backend.repository.issue.IssueRepository
import dev.bogwalk.bootrack.backend.repository.notification.NotificationRepository
import dev.bogwalk.bootrack.backend.repository.project.ProjectRepository
import dev.bogwalk.bootrack.backend.repository.user.UserRepository

/**
 * Main representation of all server-side operations for defining & manipulating stored data.
 */
internal class BooTrackRepository : DatabaseFactory {
    val userRepository: UserRepository = UserRepository()

    val projectRepository: ProjectRepository = ProjectRepository()

    val issueRepository : IssueRepository = IssueRepository()

    val commentRepository : CommentRepository = CommentRepository()

    val notificationRepository : NotificationRepository = NotificationRepository()
}
