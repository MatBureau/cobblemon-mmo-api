package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.BattlePassService
import com.cobblemon.mmo.common.dto.requests.AddBattlePassXpRequest
import com.cobblemon.mmo.common.dto.responses.ApiResponse
import com.cobblemon.mmo.common.dto.responses.BattlePassResponse
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.battlePassRoutes() {
    val battlePassService by inject<BattlePassService>()

    authenticate("server-auth") {
        route("/api/battlepass") {
            get("/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val season = call.request.queryParameters["season"]?.toIntOrNull() ?: 1
                val progress = battlePassService.getProgress(playerUuid, season)
                val xpPerTier = 1000L
                call.respond(ApiResponse.ok(BattlePassResponse(
                    playerUuid = playerUuid,
                    seasonNumber = progress.seasonNumber,
                    currentTier = progress.currentTier,
                    currentXp = progress.currentXp,
                    xpToNextTier = xpPerTier - progress.currentXp,
                    isPremium = progress.isPremium,
                    claimedTiers = progress.claimedTiers,
                )))
            }

            post("/{playerUuid}/xp") {
                val playerUuid = call.parameters["playerUuid"]!!
                val req = call.receive<AddBattlePassXpRequest>()
                val result = battlePassService.addXp(playerUuid, 1, req.xp, req.source)
                call.respond(ApiResponse.ok(mapOf("newTier" to result.currentTier, "currentXp" to result.currentXp)))
            }

            post("/{playerUuid}/claim/{tier}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val tier = call.parameters["tier"]!!.toInt()
                battlePassService.claimReward(playerUuid, 1, tier)
                call.respond(ApiResponse.ok(mapOf("claimed" to tier)))
            }

            post("/{playerUuid}/upgrade") {
                val playerUuid = call.parameters["playerUuid"]!!
                battlePassService.upgradeToPremium(playerUuid, 1)
                call.respond(ApiResponse.ok(mapOf("premium" to true)))
            }
        }
    }
}
