package dev.bogwalk.bootrack.backend.schema.tables

import dev.bogwalk.bootrack.backend.schema.custom.*
import dev.bogwalk.bootrack.model.IssuePriority
import dev.bogwalk.bootrack.model.IssueState
import org.jetbrains.exposed.v1.core.Sequence
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

internal object Issues : CompositeIdTable("issues") {
    val number = long("number")
        .autoIncrement(Sequence("custom_seq", startWith = 100))
        .entityId()
    val projectId = integer("project_id").entityId().references(Projects.id)

    val projectCode = varchar("project_code", 5)
    // Column for storing database-generated strings of format XXXX-###, based on other column values
    val code = varchar("code", 32)
        .withDefinition("GENERATED ALWAYS AS (", ConcatOp("-", projectCode, number), ") STORED")
        .databaseGenerated()

    val authorId = reference("author_id", Users)
    val assigneeId = optReference("assignee_id", Users)

    val title = text("title")
    val description = text("description")

    val priority = enumerationByName<IssuePriority>("priority", 16)
    val state = enumerationByName<IssueState>("state", 16)

    val watchers = array<Int>("watchers")
    val upvotes = array<Int>("upvotes").default(emptyList())

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val modifiedAt = timestamp("modified_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(number, projectId)
}
