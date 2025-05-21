package dev.bogwalk.bootrack.client.storage

import dev.bogwalk.bootrack.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Client-side local storage for short-term persistence of data already loaded from the server.
 */
class AppCache : LocalCache {
    private val users = MutableStateFlow<List<User>>(emptyList())

    private val projects = MutableStateFlow<List<Project>>(emptyList())

    private val issuesSummarized = MutableStateFlow<List<IssueSummarized>>(emptyList())

    private val rankedIssues = MutableStateFlow<List<List<IssueSummarized>>>(emptyList())

    private val issuesDetailed = MutableStateFlow<List<IssueDetailed>>(emptyList())

    private val notifications = MutableStateFlow<List<SessionNotification>>(emptyList())

    override fun getUsers(): StateFlow<List<User>> = users.asStateFlow()

    override suspend fun setUsers(body: (List<User>) -> List<User>) {
        users.update(body)
    }

    override fun findUser(id: Int): IndexedValue<User>? = users.value
        .withIndex()
        .find { it.value.id == id }

    override fun findUser(username: String): IndexedValue<User>? = users.value
        .withIndex()
        .find { it.value.username == username }

    override fun getProjects(): StateFlow<List<Project>> = projects.asStateFlow()

    override suspend fun setProjects(body: (List<Project>) -> List<Project>) {
        projects.update(body)
    }

    override fun findProject(id: Int): IndexedValue<Project>? = projects.value
        .withIndex()
        .find { it.value.id == id }

    override fun findProject(code: String): IndexedValue<Project>? = projects.value
        .withIndex()
        .find { it.value.code == code }

    override fun countIssuesSummary(): Int = issuesSummarized.value.size

    override fun getIssuesSummary(): StateFlow<List<IssueSummarized>> = issuesSummarized.asStateFlow()

    override suspend fun setIssuesSummary(body: (List<IssueSummarized>) -> List<IssueSummarized>) {
        issuesSummarized.update(body)
    }

    override fun findIssueSummarized(code: String): IndexedValue<IssueSummarized>? = issuesSummarized.value
        .withIndex()
        .find { it.value.issue.code == code }

    override fun getRankedIssues(): StateFlow<List<List<IssueSummarized>>> = rankedIssues.asStateFlow()

    override fun setRankedIssues(body: (List<List<IssueSummarized>>) -> List<List<IssueSummarized>>) {
        rankedIssues.update(body)
    }

    override fun findRankedIssue(code: String): Pair<Int, IndexedValue<IssueSummarized>>? {
        if (rankedIssues.value.isEmpty()) return null

        var outerIndex = 0
        var found: IndexedValue<IssueSummarized>? = null

        while (outerIndex < 3) {
            found = rankedIssues.value
                .getOrNull(outerIndex)
                ?.withIndex()
                ?.find { it.value.issue.code == code }
            if (found != null) break
            outerIndex++
        }

        return found?.let { outerIndex to it }
    }

    override fun getIssuesDetailed(): StateFlow<List<IssueDetailed>> = issuesDetailed.asStateFlow()

    override suspend fun setIssuesDetailed(body: (List<IssueDetailed>) -> List<IssueDetailed>) {
        issuesDetailed.update(body)
    }

    override fun findIssueDetailed(code: String): IndexedValue<IssueDetailed>? = issuesDetailed.value
        .withIndex()
        .find { it.value.issue.code == code }

    override fun getNotifications(): StateFlow<List<SessionNotification>> = notifications.asStateFlow()

    override suspend fun setNotifications(body: (List<SessionNotification>) -> List<SessionNotification>) {
        notifications.update(body)
    }

    override fun findNotification(id: Long): IndexedValue<SessionNotification>? = notifications.value
        .withIndex()
        .find { it.value.id == id }
}