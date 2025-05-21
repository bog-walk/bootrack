package dev.bogwalk.bootrack.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun createHttpClient(): HttpClient = HttpClient {
    install(Resources)

    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
            }
        )
    }

    defaultRequest {
        host = "0.0.0.0"
        port = 8080
        url { protocol = URLProtocol.HTTP }
    }

    expectSuccess = true
}
