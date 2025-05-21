package dev.bogwalk.bootrack.backend.repository.project

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.model.Project

/**
 * Base representation of all server-side operations for defining and manipulating stored [Project] data.
 */
internal interface ProjectRepository : BaseRepository {
    /** Queries all stored [Project] records. */
    suspend fun getAllProjects(): List<Project>
}