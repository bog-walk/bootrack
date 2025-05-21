package dev.bogwalk.bootrack.backend.repository.issue

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.model.*

/**
 * Base representation of all server-side operations for defining and manipulating stored [Issue] data.
 */
internal interface IssueRepository : BaseRepository {
    /** Counts the stored [Issue] records that reference the project with the specified [projectId]. */
    suspend fun countIssuesInProject(projectId: Int): Int

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId].
     *
     * Pass arguments to [limit] and [offset] to either reduce the initial data loaded, or retrieve only a subset of all stored issues.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int, offset: Int): List<IssueSummarized>

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId], using cursor pagination,
     * with a [limit] of retrieved records as long as the iterator has more results.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int): List<IssueSummarized>

    /**
     * Queries the stored [Issue] record with the specified [number] and [projectId].
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(number: Long, projectId: Int): IssueDetailed?

    /**
     * Queries the stored [Issue] record with the specified [offset] position.
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(projectId: Int, offset: Int): IssueDetailed?

    /**
     * Counts the stored [Issue] records that reference the project with the specified [projectId] and contain a match
     * for the specified [search] parameter.
     *
     * If `hideResolved` is set to `true`, any issues with the state `IssueState.COMPLETED` will be excluded.
     */
    suspend fun countFilteredIssues(search: String, hideResolved: Boolean, projectId: Int): Int

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId] and contain a match
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
     * Queries all stored [Issue] records that reference the project with the specified [projectId] and are within [maxDistance]
     * from the [targetLocation].
     *
     * All issues that match the location condition will be returned, unless their state is `IssueState.COMPLETED`.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun filterIssuesByDistance(projectId: Int, targetLocation: Location, maxDistance: Int): List<IssueSummarized>

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId] and that do not have
     * the state `IssueState.COMPLETED`. Results are ordered by the specified condition [orderBy].
     *
     * Only the top 3 (maximum) records from each groupy will be retrieved.
     * `orderBy` parameter can be either `open` or `upvotes` or `stars`.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun rankIssues(orderBy: String, projectId: Int): List<List<IssueSummarized>>

    /**
     * Inserts a new [Issue] record.
     *
     * @return The newly created [Issue] with its database-generated number and timestamps.
     */
    suspend fun addIssue(issue: Issue): Issue

    /**
     * Updates a stored [Issue] record.
     *
     * @return The updated [Issue] with its database-generated timestamp.
     */
    suspend fun editIssue(issue: Issue): Issue

    /**
     * Updates a stored [Issue] record to have its array elements toggled with the specified [userId].
     * If a matching record is not found, the issue is inserted.
     *
     * @return The updated [Issue] with its database-generated timestamp.
     */
    suspend fun editIssue(issue: Issue, userId: Int, toggle: String): Issue

    /**
     * Deletes a stored [Issue] record with the specified [number] and [projectId].
     *
     * @return Whether the delete operation was successful.
     */
    suspend fun deleteIssue(number: Long, projectId: Int): Boolean
}