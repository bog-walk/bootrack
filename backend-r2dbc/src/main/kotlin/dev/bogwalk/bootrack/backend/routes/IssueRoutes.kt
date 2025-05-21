package dev.bogwalk.bootrack.backend.routes

import dev.bogwalk.bootrack.backend.repository.issue.IssueRepository
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.routes.Projects
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.toBoolean
import kotlin.text.toInt

/** Route handler for the nested `Issues` resource. */
internal fun Route.issueRoutes(repository: IssueRepository) {
    get<Projects.Id.Issues> {
        val qp = call.request.queryParameters
        val limit= qp["limit"]?.toInt() ?: 0
        val offset = qp["offset"]?.toInt() ?: 0
        val searchText = qp["searchText"] ?: ""
        val hideResolved = qp["hideResolved"]?.toBoolean() == true
        val sortBy = qp["sortBy"] ?: ""

        if (limit == 1) {
            val issue = repository.getIssue(it.parent.projectId, offset)
            if (issue == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(issue)
        } else {
            val issues = when {
                searchText.isNotEmpty() -> repository.filterIssues(searchText, hideResolved, sortBy, it.parent.projectId, limit, offset)
                offset == -1 -> repository.getIssuesByProject(it.parent.projectId, limit)
                else -> repository.getIssuesByProject(it.parent.projectId, limit, offset)
            }
            call.respond(issues)
        }
    }

    get<Projects.Id.Issues.Count> {
        val qp = call.request.queryParameters
        val searchText = qp["searchText"] ?: ""
        val hideResolved = qp["hideResolved"]?.toBoolean() == true
        val count = if (searchText.isEmpty()) {
            repository.countIssuesInProject(it.parent.parent.projectId)
        } else {
            repository.countFilteredIssues(searchText, hideResolved, it.parent.parent.projectId)
        }
        call.respond(count)
    }

    get<Projects.Id.Issues.Rank> {
        val qp = call.request.queryParameters
        val orderBy = qp["orderBy"] ?: ""
        val issues = repository.rankIssues(orderBy, it.parent.parent.projectId)
        call.respond(issues)
    }

    get<Projects.Id.Issues.Distance> {
        call.respond(emptyList<IssueSummarized>())
    }

    get<Projects.Id.Issues.Number> {
        val issue = repository.getIssue(it.issueNumber, it.parent.parent.projectId)
        if (issue == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }
        call.respond(issue)
    }

    post<Projects.Id.Issues> {
        val issue = call.receive<Issue>()
        try {
            val created = repository.addIssue(issue)
            call.respond(HttpStatusCode.Created, created)
        } catch (cause: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    put<Projects.Id.Issues.Number> {
        val updated = call.receive<Issue>()
        val qp = call.request.queryParameters
        val userId = qp["userId"]?.toInt()
        val toggle = qp["toggle"] ?: ""

        try {
            val dbUpdated = if (userId == null) {
                repository.editIssue(updated)
            } else {
                repository.editIssue(updated, userId, toggle)
            }
            call.respond(HttpStatusCode.OK, dbUpdated)
        } catch (cause: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    delete<Projects.Id.Issues.Number> {
        if (repository.deleteIssue(it.issueNumber, it.parent.parent.projectId)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
