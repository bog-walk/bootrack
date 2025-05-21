package dev.bogwalk.bootrack.backend.repository.project

import dev.bogwalk.bootrack.backend.schema.entities.ProjectEntity
import dev.bogwalk.bootrack.model.Project

/**
 * Represents all data access operations involving [Project] records using the DAO approach.
 */
internal class DAOProjectRepository : ProjectRepository {
    override suspend fun getAllProjects(): List<Project> = query {
        ProjectEntity
            .all()
            .map(ProjectEntity::toProject)
    }
}
