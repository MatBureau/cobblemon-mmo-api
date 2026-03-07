package com.cobblemon.mmo.api.middleware

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureRequestLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
        format { call ->
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val status = call.response.status()?.value ?: "unknown"
            val duration = call.processingTimeMillis()
            "[${method}] $path -> $status (${duration}ms)"
        }
    }
}
