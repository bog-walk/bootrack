package dev.bogwalk.bootrack.backend.schema.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

internal object Comments : LongIdTable("comments") {
    val issueNumber = long("issue_number")
    val projectId = integer("project_id")

    val authorId = reference("author_id", Users)

    val content = text("content")

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val modifiedAt = timestamp("modified_at").defaultExpression(CurrentTimestamp)

    init {
        foreignKey(
            issueNumber,
            projectId,
            target = Issues.primaryKey,
            onDelete = ReferenceOption.CASCADE,
        )
    }
}