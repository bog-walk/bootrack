package dev.bogwalk.bootrack.backend.schema.tables

import dev.bogwalk.bootrack.model.UserSettings
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.json.json

internal object Users : IntIdTable("users") {
    val fullName = varchar("full_name", 128)
    val username = varchar("username", 128)

    val settings = json<UserSettings>("settings", Json.Default)
}
