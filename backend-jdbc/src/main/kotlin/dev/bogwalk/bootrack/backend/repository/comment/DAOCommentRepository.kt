package dev.bogwalk.bootrack.backend.repository.comment

import dev.bogwalk.bootrack.backend.schema.entities.CommentEntity
import dev.bogwalk.bootrack.backend.schema.entities.IssueEntity
import dev.bogwalk.bootrack.backend.schema.entities.UserEntity
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.model.Comment
import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.core.dao.id.CompositeID

/**
 * Represents all data access operations involving [Comment] records using the DAO approach.
 */
internal class DAOCommentRepository : CommentRepository {
    override suspend fun addComment(comment: Comment): Comment = query {
        val parentIssue = IssueEntity.findById(
            CompositeID {
                it[Issues.projectId] = comment.projectId
                it[Issues.number] = comment.issueNumber
            }
        )
            ?: error("Could not find an associated IssueEntity for CommentEntity creation")

        val newComment = CommentEntity.new {
            author = UserEntity.findById(comment.authorId) ?: error("Could not find an associated UserEntity for CommentEntity creation")
            content = comment.content
            issue = parentIssue
        }

        val createdComment = newComment.let(CommentEntity::toComment)

        // sync Issues table to reflect modified TS in Comments table

        parentIssue.modifiedAt = newComment.createdAt

        createdComment
    }

    override suspend fun editComment(comment: Comment): Comment = query {
        val entity = CommentEntity.findByIdAndUpdate(comment.id) {
            it.content = comment.content
            // no need to update modified_at since Comments table has associated trigger
        }

        entity?.refresh(flush = true)

        entity
            ?.let(CommentEntity::toComment)
            ?: error("Entity not found for issue update")
    }

    override suspend fun deleteComment(id: Long): Boolean = query {
        val entity = CommentEntity.findById(id)

        val parentIssue = entity?.issue

        entity?.delete()
        // sync Issues table
        parentIssue?.modifiedAt = Clock.System.now()

        entity == null || (CommentEntity.testCache(entity.id) == null && parentIssue != null)
    }
}
