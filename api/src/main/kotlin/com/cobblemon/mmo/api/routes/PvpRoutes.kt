package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.EloService
import com.cobblemon.mmo.api.services.MatchmakingService
import com.cobblemon.mmo.api.repositories.PvpRepository
import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.common.dto.requests.JoinPvpQueueRequest
import com.cobblemon.mmo.common.dto.requests.PvpResultRequest
import com.cobblemon.mmo.common.dto.responses.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.pvpRoutes() {
    val matchmakingService by inject<MatchmakingService>()
    val eloService by inject<EloService>()
    val pvpRepository by inject<PvpRepository>()
    val appConfig by inject<AppConfig>()

    authenticate("server-auth") {
        route("/api/pvp") {
            post("/queue") {
                val req = call.receive<JoinPvpQueueRequest>()
                // In production, fetch player's current ELO from PvpRepository
                val elo = pvpRepository.getPlayerStats(req.playerUuid, 1)?.elo
                    ?: appConfig.game.pvp.defaultElo
                matchmakingService.joinQueue(req.playerUuid, elo)
                call.respond(ApiResponse.ok(mapOf("queued" to true, "elo" to elo)))
            }

            delete("/queue/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                matchmakingService.leaveQueue(playerUuid)
                call.respond(ApiResponse.ok(mapOf("removed" to true)))
            }

            post("/result") {
                val req = call.receive<PvpResultRequest>()
                val seasonNumber = 1 // In production, get from active season config
                eloService.recordMatchResult(
                    player1Uuid = req.player1Uuid,
                    player2Uuid = req.player2Uuid,
                    winnerUuid = req.winnerUuid,
                    seasonNumber = seasonNumber,
                    durationSeconds = req.durationSeconds,
                    serverName = req.serverName,
                )
                call.respond(ApiResponse.ok(mapOf("recorded" to true)))
            }

            get("/rankings") {
                val season = call.request.queryParameters["season"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                val rankings = pvpRepository.getLeaderboard(season, limit)
                val response = rankings.mapIndexed { index, row ->
                    PvpRankingResponse(
                        rank = index + 1,
                        playerUuid = row.playerUuid,
                        username = row.playerUuid, // In production, join with Players table
                        elo = row.elo,
                        wins = row.wins,
                        losses = row.losses,
                        seasonNumber = row.seasonNumber,
                    )
                }
                call.respond(ApiResponse.ok(response))
            }

            get("/player/{uuid}/stats") {
                val uuid = call.parameters["uuid"]!!
                val season = call.request.queryParameters["season"]?.toIntOrNull() ?: 1
                val stats = pvpRepository.getPlayerStats(uuid, season)
                    ?: pvpRepository.getOrCreateRanking(uuid, season, appConfig.game.pvp.defaultElo)
                val winRate = if (stats.wins + stats.losses > 0)
                    stats.wins.toDouble() / (stats.wins + stats.losses) else 0.0
                call.respond(ApiResponse.ok(PvpStatsResponse(
                    playerUuid = uuid,
                    elo = stats.elo,
                    wins = stats.wins,
                    losses = stats.losses,
                    draws = stats.draws,
                    winRate = winRate,
                    seasonNumber = stats.seasonNumber,
                )))
            }
        }
    }
}
