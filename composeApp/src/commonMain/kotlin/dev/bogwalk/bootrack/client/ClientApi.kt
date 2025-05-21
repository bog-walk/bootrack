package dev.bogwalk.bootrack.client

import dev.bogwalk.bootrack.model.*

/**
 * Base representation of all client-side operations for defining and manipulating stored data.
 */
interface ClientApi {
    // miscellaneous

    /** Shuts down the server and closes the underlying HttpClient engine. */
    suspend fun cleanUp()

    // user-level

    /** Loads data for all stored [User]s. */
    suspend fun getAllUsers(): List<User>

    /** Loads data for a [User] retrieved by its [id]. */
    suspend fun getUser(id: Int): User

    /** Edits a [User] data record. */
    suspend fun editUser(user: User)

    // project-level

    /** Loads data for all stored [Project]s. */
    suspend fun getAllProjects(): List<Project>

    // issue-level

    /** Returns a count of the stored [Issue] records that reference the project with the specified [projectId]. */
    suspend fun countIssuesInProject(projectId: Int): Int

    /**
     * Loads data for all stored [Issue] records that reference the project with the specified [projectId].
     *
     * Pass arguments to [limit] and [offset] to either reduce the initial data loaded, or retrieve only a subset of all stored issues.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int, offset: Int): List<IssueSummarized>

    /**
     * Loads data for all stored [Issue] records that reference the project with the specified [projectId], using cursor pagination,
     * with a [limit] of retrieved records as long as the iterator has more results.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int): List<IssueSummarized>

    /**
     * Loads data for an [Issue] retrieved by its [number] and [projectId].
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(number: Long, projectId: Int): IssueDetailed

    /**
     * Loads data for a single [Issue] retrieved by its [offset] position.
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(projectId: Int, offset: Int): IssueDetailed

    /**
     * Returns a count of the stored [Issue] records that reference the project with the specified [projectId] and contain a match
     * for the specified [search] parameter.
     *
     * If `hideResolved` is set to `true`, any issues with the state `IssueState.COMPLETED` will be excluded.
     */
    suspend fun countFilteredIssues(search: String, hideResolved: Boolean, projectId: Int): Int

    /**
     * Loads data for all stored [Issue] records that reference the project with the specified [projectId] and contain a match
     * for the specified [search] parameter.
     *
     * If `hideResolved` is set to `true`, any issues with the state `IssueState.COMPLETED` will be excluded.
     * `sortBy` parameter can be either `relevance` or `updated`.
     *
     * Pass arguments to [limit] and [offset] to either reduce the initial data loaded, or retrieve only a subset of all stored issues.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun filterIssues(search: String, hideResolved: Boolean, sortBy: String, projectId: Int, limit: Int, offset: Int): List<IssueSummarized>

    /**
     * Loads data for all stored [Issue] records that reference the project with the specified [projectId] and are within [maxDistance]
     * from the [targetLocation].
     *
     * All issues that match the location condition will be returned, unless their state is `IssueState.COMPLETED`.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun filterIssuesByDistance(projectId: Int, targetLocation: Location, maxDistance: Int): List<IssueSummarized>

    /**
     * Loads data for all stored [Issue] records that reference the project with the specified [projectId] and that do not have
     * the state `IssueState.COMPLETED`. Results are ordered by the specified condition [orderBy].
     *
     * Only the records ranked 2 or more from each groupy will be retrieved.
     * `orderBy` parameter can be either `open` or `upvotes` or `stars`.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun rankIssues(orderBy: String, projectId: Int): List<List<IssueSummarized>>

    /**
     * Stores a new [Issue] record.
     *
     * @return The newly created [Issue] with its database-generated number and timestamps.
     */
    suspend fun addIssue(issue: Issue): Issue

    /**
     * Edits an [Issue] data record.
     *
     * @return The updated [Issue] with its database-generated timestamp.
     */
    suspend fun editIssue(updated: Issue): Issue

    /**
     * Edits an [Issue] data record to have its array elements toggled with the specified [userId].
     * If a matching record is not found, the issue is created anew.
     *
     * @return The updated [Issue] without any change in stored timestamp. This represents a silent update or toggle.
     */
    suspend fun editIssue(updated: Issue, userId: Int, toggle: String): Issue

    /** Deletes a stored [Issue] with the specified [number] and [projectId]. */
    suspend fun deleteIssue(number: Long, projectId: Int)

    // comment-level

    /**
     * Stores a new [Comment] record.
     *
     * @return The newly created [Comment] with its database-generated id and timestamps.
     */
    suspend fun addComment(comment: Comment): Comment

    /**
     * Edits a [Comment] data record.
     *
     * @return The updated [Comment] with its database-generated timestamp.
     */
    suspend fun editComment(updated: Comment): Comment

    /** Deletes a stored [Comment] with the specified [id], issue [number], and [projectId]. */
    suspend fun deleteComment(id: Long, number: Long, projectId: Int)

    // notification-level

    /**
     * Loads data for all stored [Notification] records that reference the user with the specified [userId].
     *
     * @return List of [SessionNotification] that only includes details relevant to the user of the active session.
     */
    suspend fun getNotificationsByReceiver(userId: Int): List<SessionNotification>

    /**
     * Stores a batch of new [Notification] records.
     */
    suspend fun addNotifications(notification: List<Notification>): List<Notification>

    /**
     * Edits a [SessionNotification] record.
     */
    suspend fun editNotification(notification: SessionNotification)
}
