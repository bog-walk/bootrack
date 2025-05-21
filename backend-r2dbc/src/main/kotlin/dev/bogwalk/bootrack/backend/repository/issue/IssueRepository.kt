package dev.bogwalk.bootrack.backend.repository.issue

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.backend.repository.custom.*
import dev.bogwalk.bootrack.backend.repository.custom.selectAll
import dev.bogwalk.bootrack.backend.repository.utils.Converters.returnRowToIssue
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToIssueSummarized
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowsToIssueDetailed
import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.Comment
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.model.IssueSummarized
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.denseRank
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.like
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.r2dbc.*

/**
 * Represents all server-side operations for defining and manipulating stored [Issue] data.
 */
internal class IssueRepository : BaseRepository {
    /** Counts the stored [Issue] records that reference the project with the specified [projectId]. */
    suspend fun countIssuesInProject(projectId: Int): Int = query {
        Issues
            .select(Issues.id)
            .where { issueProjectMatches(projectId) }
            .count()
            .toInt()
    }

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId].
     *
     * Pass arguments to [limit] and [offset] to either reduce the initial data loaded, or retrieve only a subset of all stored issues.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int, offset: Int): List<IssueSummarized> = query {
        queryWithPagination(limit, offset) { issueProjectMatches(projectId) }
    }

    /**
     * Queries all stored [Issue] records that reference the project with the specified [projectId], using cursor pagination,
     * with a [limit] of retrieved records as long as the iterator has more results.
     *
     * @return List of [IssueSummarized] that also includes the count of referencing [Comment]s. The actual body
     * of the issue description & comment contents are not initially loaded.
     */
    suspend fun getIssuesByProject(projectId: Int, limit: Int): List<IssueSummarized> = query {
        val (query, subQueryCount) = getIssueQuery()
        val refreshIterator = batchIterator == null || batchQuery?.where != issueProjectMatches(projectId)

        if (refreshIterator) {
            query
                .adjustWhere {
                    issueProjectMatches(projectId)
                }

            batchQuery = query
            batchIterator = batchQuery
                ?.fetchBatchedResults(batchSize = limit)
                ?.map { it.toList() }
                ?.toList()
                ?.iterator()
        }

        batchIterator?.let {
            if (!it.hasNext()) {
                batchIterator = null
                batchQuery = null
                null
            } else {
                it.next().map { rowToIssueSummarized(it, subQueryCount) }
            }
        }.orEmpty()
    }

    /**
     * Queries the stored [Issue] record with the specified [number] and [projectId].
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(number: Long, projectId: Int): IssueDetailed? = query {
        (Projects rightJoin Issues)
            .join(
                otherTable = Comments,
                joinType = JoinType.LEFT,
                onColumn = Issues.projectId,
                otherColumn = Comments.projectId,
                additionalConstraint = { Issues.number eq Comments.issueNumber }
            )
            .select(Issues.fields + Comments.columns)
            .where { issueKeysMatch(projectId, number) }
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.let(::rowsToIssueDetailed)
    }

    /**
     * Queries the stored [Issue] record with the specified [offset] position.
     *
     * @return [IssueDetailed] that includes the full body of the issue description, as well as all comments with their contents.
     */
    suspend fun getIssue(projectId: Int, offset: Int): IssueDetailed? = query {
        val issueWithProject = Projects
            .joinQuery(
                on = { Projects.id eq it[Issues.projectId] },
                joinType = JoinType.RIGHT,
            ) {
                Issues
                    .selectAll()
                    .where { issueProjectMatches(projectId) }
                    .orderBy(Issues.modifiedAt, SortOrder.DESC)
                    .limit(1)
                    .offset(offset.toLong())
            }

        val qAlias = issueWithProject.lastQueryAlias
            ?: error("Issues query not joined properly to projects table")

        val aliasedIssueColumns = Issues.columns.map { qAlias[it] }

        issueWithProject.join(
            otherTable = Comments,
            joinType = JoinType.LEFT,
            onColumn = qAlias[Issues.projectId],
            otherColumn = Comments.projectId,
            additionalConstraint = { qAlias[Issues.number] eq Comments.issueNumber }
        )
            .select(aliasedIssueColumns + Comments.columns)
            .where {
                qAlias[Issues.projectId] eq projectId
            }
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.let {
                rowsToIssueDetailed(it, qAlias)
            }
    }

    /**
     * Counts the stored [Issue] records that reference the project with the specified [projectId] and contain a match
     * for the specified [search] parameter.
     *
     * If `hideResolved` is set to `true`, any issues with the state `IssueState.COMPLETED` will be excluded.
     */
    suspend fun countFilteredIssues(search: String, hideResolved: Boolean, projectId: Int): Int = query {
        Issues
            .select(Issues.id)
            .where {
                issueProjectMatches(projectId) and
                        issueStateMatches(hideResolved) and
                        (Issues.title like "%$search%")
            }
            .count()
            .toInt()
    }

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
    suspend fun filterIssues(search: String, hideResolved: Boolean, sortBy: String, projectId: Int, limit: Int, offset: Int): List<IssueSummarized> = query {
        val (query, subQueryCount) = getIssueQuery()

        query
            .adjustWhere {
                issueProjectMatches(projectId) and issueStateMatches(hideResolved) and
                        (Issues.title like "%$search%")
            }

        query
            .orderBy(Issues.modifiedAt, SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map {
                rowToIssueSummarized(it, subQueryCount, null)
            }
            .toList()
    }

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
    suspend fun rankIssues(orderBy: String, projectId: Int): List<List<IssueSummarized>> = query {
        val starsCount = Coalesce(Issues.watchers.arrayLength(), intParam(0))
        val upvotesCount = Coalesce(Issues.upvotes.arrayLength(), intParam(0))
        val durationOpen = (CurrentTimestamp subtract Issues.createdAt)

        val ranking = denseRank()
            .over()
            .partitionBy(Issues.priority)
            .apply {
                when (orderBy) {
                    "stars" -> orderBy(starsCount, SortOrder.DESC)
                    "upvotes" -> orderBy(upvotesCount, SortOrder.DESC)
                    "open" -> orderBy(durationOpen, SortOrder.DESC)
                    else -> error("Invalid orderBY string: $orderBy")
                }
            }
            .alias("ranking")

        val (query, subQueryCount) = getIssueQuery()

        query
            .adjustSelect { select(it.fields + ranking) }
            .adjustWhere {
                issueProjectMatches(projectId) and issueStateMatches(hideResolved = true)
            }

        val queryWithRanking = With(
            name = "issues_with_ranking",
            query = query
        )
        queryWithRanking
            .selectAll()
            .where { ranking.aliasOnlyExpression() lessEq longParam(2) }
            .map {
                rowToIssueSummarized(it, subQueryCount, altSource = queryWithRanking)
            }
            .toList()
            .groupBy { it.issue.priority }
            .values
            .toList()
    }

    /**
     * Inserts a new [Issue] record.
     *
     * @return The newly created [Issue] with its database-generated number and timestamps.
     */
    suspend fun addIssue(issue: Issue): Issue = query {
        val dbGeneratedColumns = listOf(Issues.number, Issues.code, Issues.createdAt, Issues.modifiedAt)
        val returnStmt = Issues
            .insertReturning(
                returning = dbGeneratedColumns
            ) {
                it[projectId] = issue.projectId
                it[projectCode] = issue.code
                it[authorId] = issue.authorId
                it[title] = issue.title
                it[description] = issue.description
                it[priority] = issue.priority
                it[state] = issue.state
                it[assigneeId] = issue.assigneeId
                it[watchers] = issue.watchers
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated values not returned on issue creation")
    }

    /**
     * Updates a stored [Issue] record.
     *
     * @return The updated [Issue] with its database-generated timestamp.
     */
    suspend fun editIssue(issue: Issue): Issue = query {
        val returnStmt = Issues
            .updateReturning(
                returning = listOf(Issues.modifiedAt),
                where = { issueKeysMatch(issue.projectId, issue.number) }
            ) {
                it[title] = issue.title
                it[description] = issue.description
                it[priority] = issue.priority
                it[state] = issue.state
                it[assigneeId] = issue.assigneeId
                it[watchers] = issue.watchers
                it[upvotes] = issue.upvotes
                it[modifiedAt] = CurrentTimestamp
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated value not returned on issue update")
    }

    /**
     * Updates a stored [Issue] record to have its array elements toggled with the specified [userId].
     * If a matching record is not found, the issue is inserted.
     *
     * @return The updated [Issue] with its database-generated timestamp.
     */
    suspend fun editIssue(issue: Issue, userId: Int, toggle: String): Issue = query {
        if (toggle !in listOf("stars", "upvotes")) error("Unsupported toggle parameter: $toggle")

        val dbGeneratedColumns = listOf(Issues.number, Issues.createdAt, Issues.modifiedAt, Issues.watchers, Issues.upvotes)
        val returnStmt = Issues
            .upsertReturning(
                returning = dbGeneratedColumns,
                where = { issueKeysMatch(issue.projectId, issue.number) },
                onUpdate = {
                    if (toggle == "stars") {
                        it[Issues.watchers] = Issues.watchers.addOrRemove(userId)
                    }
                    if (toggle == "upvotes") {
                        it[Issues.upvotes] = Issues.upvotes.addOrRemove(userId)
                        it[Issues.watchers] = insertValue(Issues.watchers)
                    }
                },
            ) {
                it[number] = issue.number
                it[projectId] = issue.projectId
                it[projectCode] = issue.code.split('-').first()
                it[authorId] = issue.authorId
                it[title] = issue.title
                it[description] = issue.description
                it[priority] = issue.priority
                it[state] = issue.state
                it[assigneeId] = issue.assigneeId
                it[watchers] = issue.watchers
                it[upvotes] = issue.upvotes
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated value not returned on issue update")
    }

    /**
     * Deletes a stored [Issue] record with the specified [number] and [projectId].
     *
     * @return Whether the delete operation was successful.
     */
    suspend fun deleteIssue(number: Long, projectId: Int): Boolean = query {
        Issues
            .deleteWhere { issueKeysMatch(projectId, number) } == 1
    }

    // utils

    private var batchQuery: Query? = null

    private var batchIterator: Iterator<Iterable<ResultRow>>? = null

    private suspend fun queryWithPagination(
        limit: Int? = null,
        offset: Int? = null,
        predicate: SqlExpressionBuilder.()-> Op<Boolean>,
    ): List<IssueSummarized> {
        val (query, subQueryCount) = getIssueQuery()

        return query
            .where(predicate)
            .orderBy(Issues.modifiedAt, SortOrder.DESC)
            .apply { if (limit != null) limit(limit) }
            .apply { if (offset != null) offset(offset.toLong()) }
            .map {
                rowToIssueSummarized(it, subQueryCount)
            }
            .toList()
    }
}
