package dev.bogwalk.bootrack.backend.routes

import dev.bogwalk.bootrack.backend.repository.comment.CommentRepository
import dev.bogwalk.bootrack.model.Comment
import dev.bogwalk.bootrack.routes.Projects
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route

/** Route handler for the nested `Comments` resource. */
internal fun Route.commentRoutes(repository: CommentRepository) {
    post<Projects.Id.Issues.Number.Comments> {
        val comment = call.receive<Comment>()
        try {
            val created = repository.addComment(comment)
            call.respond(HttpStatusCode.Created, created)
        } catch (cause: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    put<Projects.Id.Issues.Number.Comments.CommentId> {
        val updated = call.receive<Comment>()
        try {
            val dbUpdated = repository.editComment(updated)
            call.respond(HttpStatusCode.OK, dbUpdated)
        } catch (cause: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    delete<Projects.Id.Issues.Number.Comments.CommentId> {
        if (repository.deleteComment(it.commentId)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
