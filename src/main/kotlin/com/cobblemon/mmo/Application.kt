package com.cobblemon.mmo

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.slf4j.event.Level

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok", version = "1.0.0"))
        }
    }
}

@Serializable
data class HealthResponse(
    val status: String,
    val version: String,
)
