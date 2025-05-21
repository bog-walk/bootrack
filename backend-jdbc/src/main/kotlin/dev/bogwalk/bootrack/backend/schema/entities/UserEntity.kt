package dev.bogwalk.bootrack.backend.schema.entities

import dev.bogwalk.bootrack.backend.schema.tables.Issues
import dev.bogwalk.bootrack.backend.schema.tables.Users
import dev.bogwalk.bootrack.model.User
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

internal class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    var fullName by Users.fullName
    var username by Users.username

    var settings by Users.settings

    val authoredIssues by IssueEntity referrersOn Issues.authorId
    val assignedIssues by IssueEntity optionalReferrersOn Issues.assigneeId

    companion object : IntEntityClass<UserEntity>(Users)

    fun toUser(): User = User(
        id = id.value,
        fullName = fullName,
        username = username,
        settings = settings,
    )
}
