package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.repository.comment.CommentRepository
import dev.bogwalk.bootrack.backend.repository.issue.IssueRepository
import dev.bogwalk.bootrack.backend.repository.notification.NotificationRepository
import dev.bogwalk.bootrack.backend.repository.project.ProjectRepository
import dev.bogwalk.bootrack.backend.repository.user.UserRepository

/**
 * Main representation of all server-side operations for defining & manipulating stored data.
 */
internal abstract class BooTrackRepository {
    abstract val userRepository: UserRepository

    abstract val projectRepository: ProjectRepository

    abstract val issueRepository : IssueRepository

    abstract val commentRepository : CommentRepository

    abstract val notificationRepository : NotificationRepository
}