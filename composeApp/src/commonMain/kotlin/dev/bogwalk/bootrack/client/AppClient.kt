package dev.bogwalk.bootrack.client

import dev.bogwalk.bootrack.model.*
import dev.bogwalk.bootrack.routes.BackendShutdown
import dev.bogwalk.bootrack.routes.Notifications
import dev.bogwalk.bootrack.routes.Projects
import dev.bogwalk.bootrack.routes.Users
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * `HttpClient` implementation for all client-side operations that rely on making HTTP requests.
 */
class AppClient : ClientApi {
    private val client = createHttpClient()

    // miscellaneous

    override suspend fun cleanUp() {
        client.get(Notifications.Sync())
        client.get(BackendShutdown())
        client.close()
    }

    // user-level

    override suspend fun getAllUsers(): List<User> {
        return client.get(Users()).body()
    }

    override suspend fun getUser(id: Int): User {
        return client.get(Users.Id(userId = id)).body<User>()
    }

    override suspend fun editUser(user: User) {
        client.put(Users.Id(userId = user.id)) {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    }

    // project-level

    override suspend fun getAllProjects(): List<Project> {
        return client.get(Projects()).body()
    }

    // issue-level

    override suspend fun countIssuesInProject(projectId: Int): Int {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.get(Projects.Id.Issues.Count(parent)).body()
    }

    override suspend fun getIssuesByProject(projectId: Int, limit: Int, offset: Int): List<IssueSummarized> {
        val parent = Projects.Id(projectId = projectId)
        return client.get(Projects.Id.Issues(parent, limit, offset)).body()
    }

    override suspend fun getIssuesByProject(projectId: Int, limit: Int): List<IssueSummarized> {
        val parent = Projects.Id(projectId = projectId)
        return client.get(Projects.Id.Issues(parent, limit, -1)).body()
    }

    override suspend fun getIssue(number: Long, projectId: Int): IssueDetailed {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.get(Projects.Id.Issues.Number(parent, number)).body()
    }

    override suspend fun getIssue(projectId: Int, offset: Int): IssueDetailed {
        val parent = Projects.Id(projectId = projectId)
        return client.get(Projects.Id.Issues(parent, 1, offset)).body()
    }

    override suspend fun countFilteredIssues(search: String, hideResolved: Boolean, projectId: Int): Int {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject, searchText = search, hideResolved = hideResolved)
        return client.get(Projects.Id.Issues.Count(parent)).body()
    }

    override suspend fun filterIssues(search: String, hideResolved: Boolean, sortBy: String, projectId: Int, limit: Int, offset: Int): List<IssueSummarized> {
        val parent = Projects.Id(projectId = projectId)
        return client.get(Projects.Id.Issues(parent, limit, offset, search, hideResolved, sortBy)).body()
    }

    override suspend fun filterIssuesByDistance(projectId: Int, targetLocation: Location, maxDistance: Int): List<IssueSummarized> {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.get(Projects.Id.Issues.Distance(parent, targetLocation, maxDistance)).body()
    }

    override suspend fun rankIssues(orderBy: String, projectId: Int): List<List<IssueSummarized>> {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.get(Projects.Id.Issues.Rank(parent, orderBy)).body()
    }

    override suspend fun addIssue(issue: Issue): Issue {
        val parent = Projects.Id(projectId = issue.projectId)
        return client.post(Projects.Id.Issues(parent)) {
            contentType(ContentType.Application.Json)
            setBody(issue)
        }.body<Issue>()
    }

    override suspend fun editIssue(updated: Issue): Issue {
        val parentProject = Projects.Id(projectId = updated.projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.put(Projects.Id.Issues.Number(parent, updated.number)) {
            contentType(ContentType.Application.Json)
            setBody(updated)
        }.body<Issue>()
    }

    override suspend fun editIssue(updated: Issue, userId: Int, toggle: String): Issue {
        val parentProject = Projects.Id(projectId = updated.projectId)
        val parent = Projects.Id.Issues(parentProject)
        return client.put(Projects.Id.Issues.Number(parent, updated.number, userId, toggle)) {
            contentType(ContentType.Application.Json)
            setBody(updated)
        }.body<Issue>()
    }

    override suspend fun deleteIssue(number: Long, projectId: Int) {
        val parentProject = Projects.Id(projectId = projectId)
        val parent = Projects.Id.Issues(parentProject)
        client.delete(Projects.Id.Issues.Number(parent, number))
    }

    // comment-level

    override suspend fun addComment(comment: Comment): Comment {
        val parentProject = Projects.Id(projectId = comment.projectId)
        val parentIssue = Projects.Id.Issues(parentProject)
        val parent = Projects.Id.Issues.Number(parentIssue, comment.issueNumber)
        return client.post(Projects.Id.Issues.Number.Comments(parent)) {
            contentType(ContentType.Application.Json)
            setBody(comment)
        }.body<Comment>()
    }

    override suspend fun editComment(updated: Comment): Comment {
        val parentProject = Projects.Id(projectId = updated.projectId)
        val parentIssue = Projects.Id.Issues.Number(Projects.Id.Issues(parentProject), updated.issueNumber)
        val parent = Projects.Id.Issues.Number.Comments(parentIssue)
        return client.put(Projects.Id.Issues.Number.Comments.CommentId(parent, updated.id)) {
            contentType(ContentType.Application.Json)
            setBody(updated)
        }.body<Comment>()
    }

    override suspend fun deleteComment(id: Long, number: Long, projectId: Int) {
        val parentProject = Projects.Id(projectId = projectId)
        val parentIssue = Projects.Id.Issues.Number(Projects.Id.Issues(parentProject), number)
        val parent = Projects.Id.Issues.Number.Comments(parentIssue)
        client.delete(Projects.Id.Issues.Number.Comments.CommentId(parent, id))
    }

    // notification-level

    override suspend fun getNotificationsByReceiver(userId: Int): List<SessionNotification> {
        return client.get(Notifications.UserId(userId = userId)).body()
    }

    override suspend fun addNotifications(notification: List<Notification>): List<Notification> {
        return client.post(Notifications()) {
            contentType(ContentType.Application.Json)
            setBody(notification)
        }.body<List<Notification>>()
    }

    override suspend fun editNotification(notification: SessionNotification) {
        client.put(Notifications.NotificationId(notificationId = notification.id)) {
            contentType(ContentType.Application.Json)
            setBody(notification)
        }
    }
}
