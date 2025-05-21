package dev.bogwalk.bootrack.backend.repository.project

import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToProject
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.Project
import org.jetbrains.exposed.v1.jdbc.selectAll

/**
 * Represents all data access operations involving [Project] records using the DSL approach.
 */
internal class DSLProjectRepository : ProjectRepository {
    override suspend fun getAllProjects(): List<Project> = query {
        Projects
            .selectAll()
            .map(::rowToProject)
    }
}
