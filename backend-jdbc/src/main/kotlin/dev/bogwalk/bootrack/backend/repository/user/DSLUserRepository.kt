package dev.bogwalk.bootrack.backend.repository.user

import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToUser
import dev.bogwalk.bootrack.backend.schema.tables.Users
import dev.bogwalk.bootrack.model.User
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

/**
 * Represents all data access operations involving [User] records using the DSL approach.
 */
internal class DSLUserRepository : UserRepository {
    override suspend fun getAllUsers(): List<User> = query {
        Users
            .selectAll()
            .map(::rowToUser)
    }

    override suspend fun getUser(id: Int): User? = query {
        Users
            .selectAll()
            .where { Users.id eq id }
            .singleOrNull()
            ?.let(::rowToUser)
    }

    override suspend fun editUser(user: User): Boolean = query {
        Users
            .update(
                where = { Users.id eq user.id }
            ) {
                it[Users.settings] = user.settings
            } == 1
    }
}