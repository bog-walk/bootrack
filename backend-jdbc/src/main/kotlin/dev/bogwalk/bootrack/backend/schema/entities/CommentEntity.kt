package dev.bogwalk.bootrack.backend.schema.entities

import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.model.Comment
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

internal class CommentEntity(id: EntityID<Long>) : LongEntity(id) {
    var issue by IssueEntity referencedOn Comments

    var author by UserEntity referencedOn Comments.authorId

    var content by Comments.content

    var createdAt by Comments.createdAt
    var modifiedAt by Comments.modifiedAt

    companion object : LongEntityClass<CommentEntity>(Comments)

    fun toComment(): Comment = Comment(
        id = id.value,
        issueNumber = issue.number,
        projectId = issue.projectId,
        authorId = author.id.value,
        content = content,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
    )
}