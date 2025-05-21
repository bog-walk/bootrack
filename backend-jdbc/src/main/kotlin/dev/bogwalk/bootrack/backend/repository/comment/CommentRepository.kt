package dev.bogwalk.bootrack.backend.repository.comment

import dev.bogwalk.bootrack.backend.repository.BaseRepository
import dev.bogwalk.bootrack.model.Comment

/**
 * Base representation of all server-side operations for defining and manipulating stored [Comment] data.
 */
internal interface CommentRepository : BaseRepository {
    /**
     * Inserts a new [Comment] record.
     *
     * @return The newly created [Comment] with its database-generated id and timestamps.
     */
    suspend fun addComment(comment: Comment): Comment

    /**
     * Updates a stored [Comment] record.
     *
     * @return The updated [Comment] with its database-generated timestamp.
     */
    suspend fun editComment(comment: Comment): Comment

    /**
     * Deletes a stored [Comment] with the specified [id].
     *
     * @return Whether the delete operation was successful.
     */
    suspend fun deleteComment(id: Long): Boolean
}
