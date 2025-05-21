package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.IssueState
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

/**
 * Base representation of shared operations common across different data record groups.
 */
internal interface BaseRepository {
    suspend fun <T> query(
        block: suspend Transaction.() -> T
    ): T = suspendTransaction(Dispatchers.IO) {
        block()
    }

    fun issueProjectMatches(id: Int): Op<Boolean> = Issues.projectId eq id

    fun issueKeysMatch(projectId: Int, number: Long): Op<Boolean> {
        return Issues.projectId eq projectId and (Issues.number eq number)
    }

    fun issueStateMatches(hideResolved: Boolean): Op<Boolean> {
        return if (hideResolved) Issues.state neq IssueState.COMPLETED else Op.TRUE
    }

    fun getIssueQuery(): Pair<Query, ExpressionWithColumnTypeAlias<Long>> {
        val commentCount = Comments.id.count().alias("comment_count")

        val doubleJoinQuery = (Issues leftJoin Projects)
            .joinQuery(
                on = { queryAlias ->
                    Issues.projectId eq queryAlias[Comments.projectId] and
                            (Issues.number eq queryAlias[Comments.issueNumber])
                },
                joinType = JoinType.LEFT,
            ) {
                Comments
                    .select(Comments.issueNumber, Comments.projectId, commentCount)
                    .groupBy(Comments.issueNumber, Comments.projectId)
            }

        val subQueryCount = doubleJoinQuery.lastQueryAlias?.let {
            Coalesce(it[commentCount], longLiteral(0)).alias("sqc")
        } ?: error("Comments query not joined properly to issues query")

        val summaryColumns = Issues.columns - Issues.description

        return doubleJoinQuery.select(summaryColumns + subQueryCount) to subQueryCount
    }
}
