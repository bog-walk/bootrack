package dev.bogwalk.bootrack.backend.schema.entities

import dev.bogwalk.bootrack.backend.schema.tables.Comments
import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.model.Issue
import dev.bogwalk.bootrack.model.IssueDetailed
import dev.bogwalk.bootrack.model.IssueSummarized
import dev.bogwalk.bootrack.model.Location
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import org.jetbrains.exposed.v1.dao.EntityBatchUpdate
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.select

internal class IssueEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    var projectCode by Issues.projectCode
    val code by Issues.code

    var author by UserEntity referencedOn Issues.authorId
    var assignee by UserEntity optionalReferencedOn Issues.assigneeId

    var title by Issues.title
    var description by Issues.description

    var priority by Issues.priority
    var state by Issues.state

    var watchers by Issues.watchers
    var upvotes by Issues.upvotes

    var createdAt by Issues.createdAt
    var modifiedAt by Issues.modifiedAt

    // Ensures that entity-field to table-column association only works with setter;
    // The entity should never attempt to get 'Issues.location' directly as the value returned is an unreadable WKB.
    private var _location by Issues.location
    var location: Location?
        get() = _readValues?.get(Issues.locationReader)
        set(value) {
            _location = value
        }

    val project by ProjectEntity referencedOn Issues.projectId
    val projectId: Int
        get() {
            val num = id.value[Issues.projectId].value
            return num
        }
    val number: Long
        get() {
            val num = id.value[Issues.number].value
            return num
        }

    val comments by CommentEntity referrersOn Comments

    val commentCount: Int
        get() = _readValues?.get(commentCountAlias)?.toInt() ?: 0

    companion object : CompositeEntityClass<IssueEntity>(Issues) {
        val subQuery: Query = Comments.alias("c")
            .run {
                select(this[Comments.id].count())
                    .where {
                        this@run[Comments.issueNumber] eq Issues.number
                    }
            }

        val commentCountAlias = wrapAsExpression<Long>(
            subQuery
        ).alias("comment_count")

        // Ensures that a subquery (for the count of referencing comment records) is always included when finding an entity;
        // Instead of relying on 'comments.count()' field, which may execute an additional query.
        override fun searchQuery(op: Op<Boolean>): Query {
            return super.searchQuery(op)
                .adjustSelect {
                    select(fields + commentCountAlias)
                }
        }

        override fun new(id: CompositeID?, init: IssueEntity.() -> Unit): IssueEntity {
            val newEntity = super.new(id, init)
            // The original 'readValue' relies on the columns from associated 'Issues' table;
            // This forces the 'ResultRow' from 'searchQuery()' is used instead, so that 'Issues.location' is never included.
            newEntity._readValues = null
            return newEntity
        }
    }

    override fun flush(batch: EntityBatchUpdate?): Boolean {
        _readValues?.let { original ->
            // Ensures that any cached inserts/updates are not flushed with the manually-included location & comment count expressions;
            // This reverts all changes above to ensure that only entity fields associated with table columns are written to the database.
            val databaseFields = original.fieldIndex.keys - Issues.locationReader - commentCountAlias + Issues.location
            val newReadValues = databaseFields.associateWith { field -> original.getOrNull(field) }
            _readValues = ResultRow.createAndFillValues(newReadValues)
        }
        return super.flush(batch)
    }

    fun toIssue(
        inFull: Boolean = true
    ): Issue = Issue(
        number = number,
        projectId = projectId,
        code = code,
        authorId = author.id.value,
        assigneeId = assignee?.id?.value,
        title = title,
        description = if (inFull) description else "",
        priority = priority,
        state = state,
        location = if (inFull) location else null,
        watchers = watchers,
        upvotes = upvotes,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
    )

    fun toIssueSummarized(
        withLocation: Boolean = false
    ): IssueSummarized = IssueSummarized(
        issue = toIssue(inFull = withLocation),
        commentCount = commentCount,
    )

    fun toIssueDetailed(): IssueDetailed = IssueDetailed(
        issue = toIssue(inFull = true),
        comments = comments.map(CommentEntity::toComment),
    )
}