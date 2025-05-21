package dev.bogwalk.bootrack.backend.repository.user

import dev.bogwalk.bootrack.backend.schema.entities.UserEntity
import dev.bogwalk.bootrack.model.User

/**
 * Represents all data access operations involving [User] records using the DAO approach.
 */
internal class DAOUserRepository : UserRepository {
    override suspend fun getAllUsers(): List<User> = query {
        UserEntity
            .all()
            .map(UserEntity::toUser)
    }

    override suspend fun getUser(id: Int): User? = query {
        UserEntity
            .findById(id)
            ?.let(UserEntity::toUser)
    }

    override suspend fun editUser(user: User): Boolean = query {
        val entity = UserEntity.findById(user.id)
        entity?.settings = user.settings

        entity != null
    }
}