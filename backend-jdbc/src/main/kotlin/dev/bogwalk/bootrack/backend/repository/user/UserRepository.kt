package dev.bogwalk.bootrack.backend.repository.user

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.model.User

/**
 * Base representation of all server-side operations for defining and manipulating stored [User] data.
 */
internal interface UserRepository : BaseRepository {
    /** Queries all stored [User] records. */
    suspend fun getAllUsers(): List<User>

    /** Queries the stored [User] record with the specified [id]. */
    suspend fun getUser(id: Int): User?

    /**
     * Updates a [User] data record.
     *
     * @return Whether the update operation was successful.
     */
    suspend fun editUser(user: User): Boolean
}