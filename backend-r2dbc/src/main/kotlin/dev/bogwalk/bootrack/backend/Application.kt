package dev.bogwalk.bootrack.backend

import dev.bogwalk.bootrack.backend.plugins.configureDatabase
import dev.bogwalk.bootrack.backend.plugins.configureRouting
import dev.bogwalk.bootrack.backend.plugins.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureSerialization()

    val repository = configureDatabase()

    configureRouting(repository)
}
