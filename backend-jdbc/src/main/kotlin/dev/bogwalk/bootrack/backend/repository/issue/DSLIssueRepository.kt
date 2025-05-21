package dev.bogwalk.bootrack.backend.repository.issue

import dev.bogwalk.bootrack.backend.repository.custom.*
import dev.bogwalk.bootrack.backend.repository.utils.Converters.returnRowToIssue
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToIssueSummarized
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowsToIssueDetailed
import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.Location
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.denseRank
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.*

/**
 * Represents all data access operations involving [Issue] records using the DSL approach.
 */
internal class DSLIssueRepository : IssueRepository {
    override suspend fun countIssuesInProject(projectId: Int): Int = query {
        Issues
            .select(Issues.id)
            .where { issueProjectMatches(projectId) }
            .count()
            .toInt()
    }

    override suspend fun getIssuesByProject(
        projectId: Int,
        limit: Int,
        offset: Int
    ): List<IssueSummarized> = query {
        queryWithPagination(limit, offset) { issueProjectMatches(projectId) }
    }

    override suspend fun getIssuesByProject(
        projectId: Int,
        limit: Int
    ): List<IssueSummarized> = query {
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

    override suspend fun getIssue(
        number: Long,
        projectId: Int
    ): IssueDetailed? = query {
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
            ?.let {
                rowsToIssueDetailed(it, Issues.locationReader)
            }
    }

    override suspend fun getIssue(
        projectId: Int,
        offset: Int
    ): IssueDetailed? = query {
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

        val aliasedIssueColumns = (Issues.columns - Issues.location).map { qAlias[it] } + qAlias[Issues.locationReader]

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
                rowsToIssueDetailed(it, qAlias[Issues.locationReader], qAlias)
            }
    }

    override suspend fun countFilteredIssues(
        search: String,
        hideResolved: Boolean,
        projectId: Int
    ): Int = query {
        Issues
            .select(Issues.id)
            .where {
                issueProjectMatches(projectId) and
                        issueStateMatches(hideResolved) and
                        (Issues.textSearchable tsMatches toTSQuery(search))
            }
            .count()
            .toInt()
    }

    override suspend fun filterIssues(
        search: String,
        hideResolved: Boolean,
        sortBy: String,
        projectId: Int,
        limit: Int,
        offset: Int
    ): List<IssueSummarized> = query {
        val (query, subQueryCount) = getIssueQuery()

        val relevance = Issues.textSearchable.tsRank(toTSQuery(search)).alias("relevance")
        if (sortBy == "relevance") {
            query.adjustSelect { select(it.fields + relevance) }
        }
        query
            .adjustWhere {
                issueProjectMatches(projectId) and issueStateMatches(hideResolved) and
                        (Issues.textSearchable tsMatches toTSQuery(search))
            }

        query
            .apply { if (sortBy == "relevance") orderBy(relevance, SortOrder.DESC) else orderBy(Issues.modifiedAt, SortOrder.DESC) }
            .limit(limit)
            .offset(offset.toLong())
            .map {
                rowToIssueSummarized(it, subQueryCount, null)
            }
    }

    override suspend fun filterIssuesByDistance(
        projectId: Int,
        targetLocation: Location,
        maxDistance: Int
    ): List<IssueSummarized> = query {
        queryWithPagination(
            additionalField = Issues.locationReader
        ) {
            issueProjectMatches(projectId) and
                    issueStateMatches(hideResolved = true) and
                    Issues.location.isNotNull() and
                    (Issues.location.withinTravelDistanceTo(targetLocation, maxDistance))
        }
    }

    override suspend fun rankIssues(
        orderBy: String,
        projectId: Int
    ): List<List<IssueSummarized>> = query {
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
            .groupBy { it.issue.priority }
            .values
            .toList()
    }

    override suspend fun addIssue(issue: Issue): Issue = query {
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
                it[location] = issue.location
                it[watchers] = issue.watchers
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated values not returned on issue creation")
    }

    override suspend fun editIssue(issue: Issue): Issue = query {
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
                it[location] = issue.location
                it[watchers] = issue.watchers
                it[upvotes] = issue.upvotes
                it[modifiedAt] = CurrentTimestamp
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated value not returned on issue update")
    }

    override suspend fun editIssue(issue: Issue, userId: Int, toggle: String): Issue = query {
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
                it[location] = issue.location
                it[watchers] = issue.watchers
                it[upvotes] = issue.upvotes
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToIssue(it, issue) }
            ?: error("Database-generated value not returned on issue update")
    }

    override suspend fun deleteIssue(number: Long, projectId: Int): Boolean = query {
        Issues
            .deleteWhere { issueKeysMatch(projectId, number) } == 1
    }

    // utils

    private var batchQuery: Query? = null

    private var batchIterator: Iterator<Iterable<ResultRow>>? = null

    private fun queryWithPagination(
        limit: Int? = null,
        offset: Int? = null,
        additionalField: Expression<Location?>? = null,
        predicate: SqlExpressionBuilder.()-> Op<Boolean>,
    ): List<IssueSummarized> {
        val (query, subQueryCount) = getIssueQuery()

        if (additionalField != null) {
            query.adjustSelect { select(it.fields + additionalField) }
        }

        return query
            .where(predicate)
            .orderBy(Issues.modifiedAt, SortOrder.DESC)
            .apply { if (limit != null) limit(limit) }
            .apply { if (offset != null) offset(offset.toLong()) }
            .map {
                rowToIssueSummarized(it, subQueryCount, additionalField)
            }
    }
}