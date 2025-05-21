package dev.bogwalk.bootrack.backend.plugins

import dev.bogwalk.bootrack.backend.repository.BooTrackRepository
import dev.bogwalk.bootrack.backend.repository.DAORepository
import dev.bogwalk.bootrack.backend.repository.DSLRepository
import io.ktor.server.application.*

internal fun Application.configureDatabase(): BooTrackRepository {
    val approach = environment.config.property("database.dataAccess").getString()
    return when (DataAccessApproach.valueOf(approach)) {
        DataAccessApproach.DSL -> DSLRepository(environment.config)
        DataAccessApproach.DAO -> DAORepository(environment.config)
    }
}

private enum class DataAccessApproach {
    DSL,
    DAO
}
