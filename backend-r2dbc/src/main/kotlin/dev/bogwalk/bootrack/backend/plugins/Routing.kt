package dev.bogwalk.bootrack.backend.plugins

import dev.bogwalk.bootrack.backend.repository.BooTrackRepository
import dev.bogwalk.bootrack.backend.routes.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

internal fun Application.configureRouting(repository: BooTrackRepository) {
    install(Resources)

    routing {
        projectRoutes(repository.projectRepository)
        userRoutes(repository.userRepository)
        issueRoutes(repository.issueRepository)
        commentRoutes(repository.commentRepository)
        notificationRoutes(repository.notificationRepository)
    }
}
