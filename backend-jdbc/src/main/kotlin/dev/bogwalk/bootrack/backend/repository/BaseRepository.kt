package dev.bogwalk.bootrack.backend.repository

import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Coalesce
import org.jetbrains.exposed.v1.core.ExpressionWithColumnTypeAlias
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.neq
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.joinQuery
import org.jetbrains.exposed.v1.core.lastQueryAlias
import org.jetbrains.exposed.v1.core.longLiteral
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

/**
 * Base representation of shared operations common across different data record groups.
 */
internal interface BaseRepository {
    suspend fun <T> query(
        block: suspend Transaction.() -> T
    ): T = newSuspendedTransaction(Dispatchers.IO) {
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

        val summaryColumns = Issues.columns - Issues.description - Issues.location

        return doubleJoinQuery.select(summaryColumns + subQueryCount) to subQueryCount
    }
}