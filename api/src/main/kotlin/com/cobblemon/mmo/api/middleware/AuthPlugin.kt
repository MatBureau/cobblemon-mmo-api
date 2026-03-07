package com.cobblemon.mmo.api.middleware

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.cobblemon.mmo.common.dto.responses.ApiResponse

/**
 * Configures two authentication mechanisms:
 * 1. "server-auth" — X-Server-Secret header (used by Minecraft plugin routes)
 * 2. "admin-auth"  — X-Admin-Token header (used by admin routes)
 */
fun Application.configureAuth(config: ApplicationConfig) {
    val serverSecret = config.property("auth.serverSecret").getString()
    val adminToken = config.property("auth.adminToken").getString()

    install(Authentication) {
        // MC Plugin authentication via shared secret header
        bearer("server-auth") {
            authenticate { tokenCredential ->
                if (tokenCredential.token == serverSecret) {
                    UserIdPrincipal("mc-server")
                } else {
                    null
                }
            }
        }

        // Admin authentication via admin token header
        bearer("admin-auth") {
            authenticate { tokenCredential ->
                if (tokenCredential.token == adminToken) {
                    UserIdPrincipal("admin")
                } else {
                    null
                }
            }
        }
    }
}
