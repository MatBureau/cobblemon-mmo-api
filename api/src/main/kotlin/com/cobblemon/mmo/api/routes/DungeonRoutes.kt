package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.DungeonService
import com.cobblemon.mmo.api.services.ResinService
import com.cobblemon.mmo.common.dto.requests.CompleteDungeonRequest
import com.cobblemon.mmo.common.dto.requests.EnterDungeonRequest
import com.cobblemon.mmo.common.dto.responses.ApiResponse
import com.cobblemon.mmo.common.dto.responses.DungeonLootResponse
import com.cobblemon.mmo.common.dto.responses.ResinResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.dungeonRoutes() {
    val resinService by inject<ResinService>()
    val dungeonService by inject<DungeonService>()

    authenticate("server-auth") {
        route("/api") {
            get("/resin/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val (current, max, minutesToNext) = resinService.getResin(playerUuid)
                call.respond(ApiResponse.ok(ResinResponse(playerUuid, current, max, minutesToNext)))
            }

            post("/dungeon/enter") {
                val req = call.receive<EnterDungeonRequest>()
                val remaining = dungeonService.enterDungeon(req.playerUuid, req.dungeonId)
                call.respond(ApiResponse.ok(mapOf("resinRemaining" to remaining)))
            }

            post("/dungeon/complete") {
                val req = call.receive<CompleteDungeonRequest>()
                val loot = dungeonService.completeDungeon(req.playerUuid, req.sessionId)
                call.respond(ApiResponse.ok(DungeonLootResponse(sessionId = req.sessionId, loot = loot)))
            }

            post("/resin/consume-item") {
                val playerUuid = call.request.headers["X-Player-UUID"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest,
                        ApiResponse.fail<Unit>("MISSING_HEADER", "X-Player-UUID required"))
                resinService.addResin(playerUuid, 20)
                call.respond(ApiResponse.ok(mapOf("added" to 20)))
            }
        }
    }
}

fun Route.breedingRoutes() {
    val breedingService by inject<com.cobblemon.mmo.api.services.BreedingService>()

    authenticate("server-auth") {
        route("/api/breeding") {
            post("/breed") {
                val req = call.receive<com.cobblemon.mmo.common.dto.requests.BreedingRequest>()
                val offspringId = breedingService.breed(req.playerUuid, req.parent1Id, req.parent2Id, req.breedingItemId)
                call.respond(ApiResponse.ok(mapOf("offspringId" to offspringId)))
            }
            get("/items/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val items = breedingService.getBreedingItems(playerUuid)
                call.respond(ApiResponse.ok(items))
            }
        }
    }
}

fun Route.skinRoutes() {
    val skinService by inject<com.cobblemon.mmo.api.services.SkinService>()

    authenticate("server-auth") {
        route("/api/skins") {
            get("/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val skins = skinService.getPlayerSkins(playerUuid)
                call.respond(ApiResponse.ok(skins))
            }
            post("/{playerUuid}/apply") {
                val playerUuid = call.parameters["playerUuid"]!!
                val req = call.receive<com.cobblemon.mmo.common.dto.requests.ApplySkinRequest>()
                skinService.applySkin(playerUuid, req.pokemonId, req.skinId)
                call.respond(ApiResponse.ok(mapOf("applied" to true)))
            }
        }
    }
}

fun Route.chatRoutes() {
    val chatService by inject<com.cobblemon.mmo.api.services.ChatService>()

    authenticate("server-auth") {
        route("/api/chat") {
            post("/mute/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val req = call.receive<com.cobblemon.mmo.common.dto.requests.ChatMuteRequest>()
                chatService.mutePlayer(playerUuid, req.durationMinutes, req.reason)
                call.respond(ApiResponse.ok(mapOf("muted" to true)))
            }
        }
    }
}
