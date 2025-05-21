package dev.bogwalk.bootrack.backend.repository.project

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToProject
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.Project
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.selectAll

/**
 * Represents all server-side operations for defining and manipulating stored [Project] data.
 */
internal class ProjectRepository : BaseRepository {
    /** Queries all stored [Project] records. */
    suspend fun getAllProjects(): List<Project> = query {
        Projects
            .selectAll()
            .map(::rowToProject)
            .toList()
    }
}
