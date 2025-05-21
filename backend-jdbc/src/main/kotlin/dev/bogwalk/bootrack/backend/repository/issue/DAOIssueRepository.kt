package dev.bogwalk.bootrack.backend.repository.issue

import dev.bogwalk.bootrack.backend.repository.custom.*
import dev.bogwalk.bootrack.backend.repository.utils.Converters.returnRowToIssue
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToIssueSummarized
import dev.bogwalk.bootrack.backend.schema.entities.IssueEntity
import dev.bogwalk.bootrack.backend.schema.entities.UserEntity
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.Location
import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.denseRank
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.upsertReturning

/**
 * Represents all data access operations involving [Issue] records using the DAO approach.
 */
internal class DAOIssueRepository : IssueRepository {
    override suspend fun countIssuesInProject(projectId: Int): Int = query {
        IssueEntity
            .find { issueProjectMatches(projectId) }
            .count()
            .toInt()
    }

    override suspend fun getIssuesByProject(
        projectId: Int,
        limit: Int,
        offset: Int
    ): List<IssueSummarized> = query {
        IssueEntity
            .find { issueProjectMatches(projectId) }
            .orderBy(Issues.modifiedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .with(IssueEntity::author, IssueEntity::assignee)
            .map(IssueEntity::toIssueSummarized)
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
        val fullId = CompositeID {
            it[Issues.number] = number
            it[Issues.projectId] = projectId
        }
        val entity = IssueEntity.findById(fullId)?.load(IssueEntity::comments)

        entity
            ?.let(IssueEntity::toIssueDetailed)
    }

    override suspend fun getIssue(
        projectId: Int,
        offset: Int
    ): IssueDetailed? = query {
        val entity = IssueEntity
            .find { issueProjectMatches(projectId) }
            .orderBy(Issues.modifiedAt to SortOrder.DESC)
            .limit(1)
            .offset(offset.toLong())
            .with(IssueEntity::comments)
            .singleOrNull()

        entity
            ?.let(IssueEntity::toIssueDetailed)
    }

    override suspend fun countFilteredIssues(
        search: String,
        hideResolved: Boolean,
        projectId: Int
    ): Int = query {
        IssueEntity
            .find {
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
        IssueEntity
            .find {
                issueProjectMatches(projectId) and
                        issueStateMatches(hideResolved = true) and
                        Issues.location.isNotNull() and
                        (Issues.location.withinTravelDistanceTo(targetLocation, maxDistance))
            }
            .orderBy(Issues.modifiedAt to SortOrder.DESC)
            .with(IssueEntity::author, IssueEntity::assignee)
            .map { it.toIssueSummarized(withLocation = true) }
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
        val partialId = CompositeID {
            it[Issues.projectId] = issue.projectId
        }
        val newIssue = IssueEntity.new(partialId) {
            projectCode = issue.code
            author = UserEntity.findById(issue.authorId) ?: error("Could not find an associated UserEntity for IssueEntity creation")
            title = issue.title
            description = issue.description
            priority = issue.priority
            state = issue.state
            assignee = issue.assigneeId?.let { UserEntity.findById(it) }
            location = issue.location
            watchers = issue.watchers
        }

        newIssue
            .let(IssueEntity::toIssue)
    }

    override suspend fun editIssue(issue: Issue): Issue = query {
        val fullId = CompositeID {
            it[Issues.number] = issue.number
            it[Issues.projectId] = issue.projectId
        }

        val entity = IssueEntity.findByIdAndUpdate(fullId) {
            it.title = issue.title
            it.description = issue.description
            it.priority = issue.priority
            it.state = issue.state
            it.assignee = issue.assigneeId?.let { aId -> UserEntity.findById(aId) }
            it.location = issue.location
            it.watchers = issue.watchers
            it.upvotes = issue.upvotes
            it.modifiedAt = Clock.System.now()
        }

        entity
            ?.let(IssueEntity::toIssue)
            ?: error("Entity not found for issue update")
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
        val fullId = CompositeID {
            it[Issues.number] = number
            it[Issues.projectId] = projectId
        }
        val entity = IssueEntity.findById(fullId)
        entity?.delete()

        entity == null || IssueEntity.testCache(entity.id) == null
    }

    // utils

    private var batchQuery: Query? = null

    private var batchIterator: Iterator<Iterable<ResultRow>>? = null
}