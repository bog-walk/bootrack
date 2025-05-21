package dev.bogwalk.bootrack.backend.routes

import dev.bogwalk.bootrack.backend.repository.project.ProjectRepository
import dev.bogwalk.bootrack.routes.Projects
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** Route handler for the `Projects` resource class. */
internal fun Route.projectRoutes(repository: ProjectRepository) {
    get<Projects> {
        val projects = repository.getAllProjects()
        call.respond(projects)
    }
}
