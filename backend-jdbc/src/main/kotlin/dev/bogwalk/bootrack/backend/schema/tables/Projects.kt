package dev.bogwalk.bootrack.backend.schema.tables

import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

internal object Projects : IntIdTable("projects") {
    val name = varchar("name", 32)
    val code = varchar("code", 5)
        .check { it.charLength().between(3, 5) }
        .uniqueIndex()
}
