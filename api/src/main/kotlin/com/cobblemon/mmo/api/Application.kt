package com.cobblemon.mmo.api

import com.cobblemon.mmo.api.config.KoinConfig
import com.cobblemon.mmo.api.database.DatabaseFactory
import com.cobblemon.mmo.api.middleware.configureAuth
import com.cobblemon.mmo.api.middleware.configureStatusPages
import com.cobblemon.mmo.api.routes.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.plugin.Koin
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // referenced in application.conf
fun Application.module() {
    // Dependency injection
    install(Koin) {
        modules(KoinConfig.appModule(environment.config))
    }

    // Database
    DatabaseFactory.init(environment.config)

    // Content negotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = false
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // CORS — allow plugin servers (all origins locked by server secret header)
    install(CORS) {
        anyHost()
        allowHeader("X-Server-Secret")
        allowHeader("X-Admin-Token")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    // Rate limiting
    install(RateLimit) {
        global {
            rateLimiter(limit = 200, refillPeriod = 60.seconds)
        }
    }

    // Call logging
    install(CallLogging) {
        level = Level.INFO
    }

    // Auth (X-Server-Secret middleware)
    configureAuth(environment.config)

    // Error handling
    configureStatusPages()

    // Routes
    routing {
        playerRoutes()
        auctionRoutes()
        pvpRoutes()
        battlePassRoutes()
        bannerRoutes()
        dungeonRoutes()
        breedingRoutes()
        skinRoutes()
        chatRoutes()
        adminRoutes()
    }
}
