package dev.bogwalk.bootrack.backend.schema.entities

import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Projects
import dev.bogwalk.bootrack.model.Project
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

internal class ProjectEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by Projects.name
    var code by Projects.code

    val issues by IssueEntity referrersOn Issues

    companion object : IntEntityClass<ProjectEntity>(Projects)

    fun toProject(): Project = Project(
        id = id.value,
        name = name,
        code = code,
    )
}