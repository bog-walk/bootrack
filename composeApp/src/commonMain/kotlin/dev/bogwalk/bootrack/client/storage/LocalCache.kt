package dev.bogwalk.bootrack.client.storage

import dev.bogwalk.bootrack.model.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Base representation of local storage for short-term persistence of data already loaded from the server.
 */
interface LocalCache {
    /** Returns the read-only state of all loaded [User]s. */
    fun getUsers(): StateFlow<List<User>>

    /** Updates the value of the underlying mutable state flow of all loaded [User]s. */
    suspend fun setUsers(body: (List<User>) -> List<User>)

    /** Returns the [User] and its index, or `null` if a user with the specified [id] is not cached. */
    fun findUser(id: Int): IndexedValue<User>?

    /** Returns the [User] and its index, or `null` if a user with the specified [username] is not cached. */
    fun findUser(username: String): IndexedValue<User>?

    /** Returns the read-only state of all loaded [Project]s. */
    fun getProjects(): StateFlow<List<Project>>

    /** Updates the value of the underlying mutable state flow of all loaded [Project]s. */
    suspend fun setProjects(body: (List<Project>) -> List<Project>)

    /** Returns the [Project] and its index, or `null` if a project with the specified [id] is not cached. */
    fun findProject(id: Int): IndexedValue<Project>?

    /** Returns the [Project] and its index, or `null` if a project with the specified [code] is not cached. */
    fun findProject(code: String): IndexedValue<Project>?

    /** Returns the size of the current read-only state of loaded (summarized) issues. */
    fun countIssuesSummary(): Int

    /** Returns the read-only state of all loaded issues, in summarized format. */
    fun getIssuesSummary(): StateFlow<List<IssueSummarized>>

    /** Updates the value of the underlying mutable state flow of all loaded issues, in summarized format. */
    suspend fun setIssuesSummary(body: (List<IssueSummarized>) -> List<IssueSummarized>)

    /** Returns the [IssueSummarized] and its index, or `null` if an issue with the specified [code] is not cached. */
    fun findIssueSummarized(code: String): IndexedValue<IssueSummarized>?

    /**
     * Returns the read-only state of all loaded issues from a saved rank search, in summarized format,
     * grouped by `IssuePriority`.
     */
    fun getRankedIssues(): StateFlow<List<List<IssueSummarized>>>

    /**
     * Updates the value of the underlying mutable state flow of all loaded ranked issues, in summarized format,
     * grouped by `IssuePriority`.
     */
    fun setRankedIssues(body: (List<List<IssueSummarized>>) -> List<List<IssueSummarized>>)

    /** Returns the [IssueSummarized] and its parent & nested index from the saved rank search, or `null` if an issue with the specified [code] is not cached. */
    fun findRankedIssue(code: String): Pair<Int, IndexedValue<IssueSummarized>>?

    /** Returns the read-only state of all loaded issues, in detailed format. */
    fun getIssuesDetailed(): StateFlow<List<IssueDetailed>>

    /** Updates the value of the underlying mutable state flow of all loaded issues, in detailed format. */
    suspend fun setIssuesDetailed(body: (List<IssueDetailed>) -> List<IssueDetailed>)

    /** Returns the [IssueDetailed] and its index, or `null` if an issue with the specified [code] is not cached. */
    fun findIssueDetailed(code: String): IndexedValue<IssueDetailed>?

    /** Returns the read-only state of all loaded [SessionNotification]s. */
    fun getNotifications(): StateFlow<List<SessionNotification>>

    /** Updates the value of the underlying mutable state flow of all loaded [SessionNotification]s. */
    suspend fun setNotifications(body: (List<SessionNotification>) -> List<SessionNotification>)

    /** Returns the [SessionNotification] and its index, or `null` if a notification with the specified [id] is not cached. */
    fun findNotification(id: Long): IndexedValue<SessionNotification>?
}
