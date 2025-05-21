package dev.bogwalk.bootrack.backend.plugins

import dev.bogwalk.bootrack.backend.repository.BooTrackRepository
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

internal fun Application.configureDatabase(): BooTrackRepository {
    val repo = BooTrackRepository()
    runBlocking {
        repo.connect(environment.config)
    }
    return repo
}
