package dev.bogwalk.bootrack.backend.repository.user

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.backend.repository.utils.Converters.rowToUser
import dev.bogwalk.bootrack.backend.schema.tables.Users
import dev.bogwalk.bootrack.model.User
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update

/**
 * Represents all server-side operations for defining and manipulating stored [User] data.
 */
internal class UserRepository : BaseRepository {
    /** Queries all stored [User] records. */
    suspend fun getAllUsers(): List<User> = query {
        Users
            .selectAll()
            .map(::rowToUser)
            .toList()
    }

    /** Queries the stored [User] record with the specified [id]. */
    suspend fun getUser(id: Int): User? = query {
        Users
            .selectAll()
            .where { Users.id eq id }
            .singleOrNull()
            ?.let(::rowToUser)
    }

    /**
     * Updates a [User] data record.
     *
     * @return Whether the update operation was successful.
     */
    suspend fun editUser(user: User): Boolean = query {
        Users
            .update(
                where = { Users.id eq user.id }
            ) {
                it[Users.settings] = user.settings
            } == 1
    }
}
