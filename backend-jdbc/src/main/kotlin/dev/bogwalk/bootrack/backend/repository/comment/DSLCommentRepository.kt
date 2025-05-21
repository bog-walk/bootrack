package dev.bogwalk.bootrack.backend.repository.comment

import dev.bogwalk.bootrack.backend.repository.utils.Converters.returnRowToComment
import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.model.Comment
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.deleteReturning
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.updateReturning

/**
 * Represents all data access operations involving [Comment] records using the DSL approach.
 */
internal class DSLCommentRepository : CommentRepository {
    override suspend fun addComment(comment: Comment): Comment = query {
        val dbGeneratedColumns = listOf(Comments.id, Comments.createdAt, Comments.modifiedAt)
        val returnStmt = Comments
            .insertReturning(
                returning = dbGeneratedColumns
            ) {
                it[authorId] = comment.authorId
                it[content] = comment.content
                it[issueNumber] = comment.issueNumber
                it[projectId] = comment.projectId
            }

        val createdComment = returnStmt
            .singleOrNull()
            ?.let { returnRowToComment(it, comment) }
            ?: error("Database-generated values not returned on comment creation")

        // sync Issues table to reflect modified TS in Comments table

        Issues.join(
            otherTable = Comments,
            joinType = JoinType.INNER,
            onColumn = Issues.projectId,
            otherColumn = Comments.projectId,
            additionalConstraint = { Issues.number eq Comments.issueNumber }
        )
            .update(
                where = { Comments.id eq createdComment.id }
            ) {
                it[Issues.modifiedAt] = Comments.createdAt
            }

        createdComment
    }

    override suspend fun editComment(comment: Comment): Comment = query {
        val returnStmt = Comments
            .updateReturning(
                returning = listOf(Comments.modifiedAt),
                where = { Comments.id eq comment.id }
            ) {
                it[content] = comment.content
                // no need to update modified_at since Comments table has associated trigger
            }

        returnStmt
            .singleOrNull()
            ?.let { returnRowToComment(it, comment) }
            ?: error("Database-generated values not returned on comment update")
    }

    override suspend fun deleteComment(id: Long): Boolean = query {
        val returnStmt = Comments
            .deleteReturning(
                returning = listOf(Comments.projectId, Comments.issueNumber),
                where = { Comments.id eq id }
            )

        val (parentProjectId, parentIssueNumber) = returnStmt
            .singleOrNull()
            ?.let { it[Comments.projectId] to it[Comments.issueNumber] }
            ?: error("Database-generated values not returned on comment delete")

        // sync Issues table

        Issues
            .update(
                where = { issueKeysMatch(parentProjectId, parentIssueNumber) }
            ) {
                it[modifiedAt] = CurrentTimestamp
            } == 1
    }
}
