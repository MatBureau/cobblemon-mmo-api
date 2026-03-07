package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.PlayerService
import com.cobblemon.mmo.common.dto.requests.BalanceUpdateRequest
import com.cobblemon.mmo.common.dto.requests.PlayerLoginRequest
import com.cobblemon.mmo.common.dto.responses.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import org.koin.ktor.ext.inject

fun Route.playerRoutes() {
    val playerService by inject<PlayerService>()

    authenticate("server-auth") {
        route("/api/players") {
            post("/login") {
                val req = call.receive<PlayerLoginRequest>()
                val player = playerService.login(req.uuid, req.username, req.serverName)
                call.respond(HttpStatusCode.OK, ApiResponse.ok(PlayerResponse(
                    uuid = player.uuid,
                    username = player.username,
                    balance = player.balance,
                    playtimeSeconds = player.playtimeSeconds,
                    firstLogin = player.firstLogin.toString(),
                    lastLogin = player.lastLogin.toString(),
                    currentServer = player.currentServer,
                )))
            }

            get("/{uuid}") {
                val uuid = call.parameters["uuid"]!!
                val player = playerService.getPlayer(uuid)
                call.respond(ApiResponse.ok(PlayerResponse(
                    uuid = player.uuid,
                    username = player.username,
                    balance = player.balance,
                    playtimeSeconds = player.playtimeSeconds,
                    firstLogin = player.firstLogin.toString(),
                    lastLogin = player.lastLogin.toString(),
                    currentServer = player.currentServer,
                )))
            }

            get("/{uuid}/balance") {
                val uuid = call.parameters["uuid"]!!
                val balance = playerService.getBalance(uuid)
                call.respond(ApiResponse.ok(BalanceResponse(uuid, balance)))
            }

            post("/{uuid}/balance") {
                val uuid = call.parameters["uuid"]!!
                val req = call.receive<BalanceUpdateRequest>()
                val newBalance = playerService.adjustBalance(uuid, req.amount)
                call.respond(ApiResponse.ok(BalanceResponse(uuid, newBalance)))
            }
        }
    }
}
