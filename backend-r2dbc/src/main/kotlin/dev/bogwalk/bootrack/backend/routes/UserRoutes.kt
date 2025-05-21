package dev.bogwalk.bootrack.backend.routes

import dev.bogwalk.bootrack.backend.repository.user.UserRepository
import dev.bogwalk.bootrack.model.User
import dev.bogwalk.bootrack.routes.Users
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route

/** Route handler for the `Users` resource class. */
internal fun Route.userRoutes(repository: UserRepository) {
    get<Users> {
        val users = repository.getAllUsers()
        call.respond(users)
    }

    get<Users.Id> {
        val user = repository.getUser(it.userId)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        call.respond(user)
    }

    put<Users.Id> {
        val updated = call.receive<User>()
        if (repository.editUser(updated)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
