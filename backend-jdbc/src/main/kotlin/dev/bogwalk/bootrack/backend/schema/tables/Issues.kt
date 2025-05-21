package dev.bogwalk.bootrack.backend.schema.tables

import dev.bogwalk.bootrack.backend.schema.custom.*
import dev.bogwalk.bootrack.model.IssuePriority
import dev.bogwalk.bootrack.model.IssueState
import dev.bogwalk.bootrack.model.Location
import org.jetbrains.exposed.v1.core.Coalesce
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.ExpressionWithColumnTypeAlias
import org.jetbrains.exposed.v1.core.Sequence
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.function
import org.jetbrains.exposed.v1.core.stringLiteral
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
    // Column for storing pre-processed searchable content using 2 columns with defined search priorities;
    // 'title' column will have higher search importance than 'description' column;
    // This column will be used to create an index for full-text search queries.
    val textSearchable = tsvector("textsearchable_index_col")
        .withDefinition(
            "GENERATED ALWAYS AS (",
            SetWeight(ToTSVector(Coalesce(title, stringLiteral(""))), stringLiteral("A")) concat
                    SetWeight(ToTSVector(Coalesce(description, stringLiteral(""))), stringLiteral("B")),
            ") STORED"
        )
        .databaseGenerated()

    val priority = enumerationByName<IssuePriority>("priority", 16)
    val state = enumerationByName<IssueState>("state", 16)

    val watchers = array<Int>("watchers")
    val upvotes = array<Int>("upvotes").default(emptyList())

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val modifiedAt = timestamp("modified_at").defaultExpression(CurrentTimestamp)

    val location = geography("location").nullable()

    // Value returned from column directly would be Well-Known Binary, which would require a parser/converter;
    // This function returns the readable Well-Known Text representation instead.
    val locationReader: ExpressionWithColumnTypeAlias<Location?>
        get() = location.function("ST_ASTEXT").alias("location_text")

    // Forces all implicit 'SELECT *' queries to use the above expression, so that the actual column is never read directly
    override val fields: List<Expression<*>>
        get() = super.fields - location + locationReader

    override val primaryKey = PrimaryKey(number, projectId)

    init {
        index(
            customIndexName = "textsearch_idx",
            columns = arrayOf(textSearchable),
            indexType = "GIN",
        )
    }
}