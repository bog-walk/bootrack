package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.repository.comment.CommentRepository
import dev.bogwalk.bootrack.backend.repository.comment.DAOCommentRepository
import dev.bogwalk.bootrack.backend.repository.issue.DAOIssueRepository
import dev.bogwalk.bootrack.backend.repository.issue.IssueRepository
import dev.bogwalk.bootrack.backend.repository.notification.DAONotificationRepository
import dev.bogwalk.bootrack.backend.repository.notification.NotificationRepository
import dev.bogwalk.bootrack.backend.repository.project.DAOProjectRepository
import dev.bogwalk.bootrack.backend.repository.project.ProjectRepository
import dev.bogwalk.bootrack.backend.repository.user.DAOUserRepository
import dev.bogwalk.bootrack.backend.repository.user.UserRepository
import io.ktor.server.config.*

/**
 * Represents all data access operations using the DAO approach.
 */
internal class DAORepository(
    config: ApplicationConfig,
    override val userRepository: UserRepository = DAOUserRepository(),
    override val projectRepository: ProjectRepository = DAOProjectRepository(),
    override val issueRepository: IssueRepository = DAOIssueRepository(),
    override val commentRepository: CommentRepository = DAOCommentRepository(),
    override val notificationRepository: NotificationRepository = DAONotificationRepository(),
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
