package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.repository.comment.CommentRepository
import dev.bogwalk.bootrack.backend.repository.comment.DSLCommentRepository
import dev.bogwalk.bootrack.backend.repository.issue.DSLIssueRepository
import dev.bogwalk.bootrack.backend.repository.issue.IssueRepository
import dev.bogwalk.bootrack.backend.repository.notification.DSLNotificationRepository
import dev.bogwalk.bootrack.backend.repository.notification.NotificationRepository
import dev.bogwalk.bootrack.backend.repository.project.DSLProjectRepository
import dev.bogwalk.bootrack.backend.repository.project.ProjectRepository
import dev.bogwalk.bootrack.backend.repository.user.DSLUserRepository
import dev.bogwalk.bootrack.backend.repository.user.UserRepository
import io.ktor.server.config.*

/**
 * Represents all data access operations using the DSL approach.
 */
internal class DSLRepository(
    config: ApplicationConfig,
    override val userRepository: UserRepository = DSLUserRepository(),
    override val projectRepository: ProjectRepository = DSLProjectRepository(),
    override val issueRepository: IssueRepository = DSLIssueRepository(),
    override val commentRepository: CommentRepository = DSLCommentRepository(),
    override val notificationRepository: NotificationRepository = DSLNotificationRepository(),
) : DatabaseFactory,
    BooTrackRepository()
{
    init {
        val dbConfig = config.config("database")
        val dbDriver = dbConfig.property("driverClassName").getString()
        val dbHost = dbConfig.property("host").getString()
        val dbName = dbConfig.property("databaseName").getString()
        val dbSampleDataPath = dbConfig.property("sampleData").getString()

        val connectionPool = createHikariDataSource(
            driver = dbDriver,
            host = dbHost,
            database = dbName,
            user = dbConfig.property("user").getString(),
            password = dbConfig.property("password").getString(),
        )
        connect(connectionPool, dbSampleDataPath)
    }
}