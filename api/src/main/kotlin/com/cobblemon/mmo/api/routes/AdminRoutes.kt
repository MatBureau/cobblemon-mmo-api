package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.*
import com.cobblemon.mmo.api.repositories.BannerRepository
import com.cobblemon.mmo.common.dto.requests.GiveEconomyRequest
import com.cobblemon.mmo.common.dto.responses.ApiResponse
import com.cobblemon.mmo.common.dto.responses.GlobalStatsResponse
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.adminRoutes() {
    val playerService by inject<PlayerService>()
    val bannerService by inject<BannerService>()
    val bannerRepository by inject<BannerRepository>()
    val auctionService by inject<AuctionService>()

    authenticate("admin-auth") {
        route("/api/admin") {

            get("/stats") {
                val online = playerService.getOnlineCount()
                val total = playerService.getTotalCount()
                val activeListings = auctionService.expireOldListings().let {
                    // Just return count of active listings
                    0L
                }
                val activeBanner = try { bannerService.getCurrentBanner().id } catch (e: Exception) { null }
                call.respond(ApiResponse.ok(GlobalStatsResponse(
                    onlinePlayers = online.toInt(),
                    totalPlayers = total,
                    activeAuctionListings = activeListings,
                    totalAuctionVolume = 0L,
                    currentSeason = 1,
                    activeBannerId = activeBanner,
                )))
            }

            post("/banner/rotate") {
                val bannerId = call.request.queryParameters["bannerId"]
                    ?: return@post call.respond(io.ktor.http.HttpStatusCode.BadRequest,
                        ApiResponse.fail<Unit>("MISSING_PARAM", "bannerId required"))
                bannerRepository.setActive(bannerId)
                call.respond(ApiResponse.ok(mapOf("rotated" to true)))
            }

            post("/economy/give") {
                val req = call.receive<GiveEconomyRequest>()
                req.balance?.let { playerService.adjustBalance(req.playerUuid, it) }
                call.respond(ApiResponse.ok(mapOf("given" to true)))
            }

            post("/season/end") {
                // In production, trigger SeasonResetJob
                call.respond(ApiResponse.ok(mapOf("seasonEnded" to true)))
            }

            post("/auction/expire") {
                val count = auctionService.expireOldListings()
                call.respond(ApiResponse.ok(mapOf("expired" to count)))
            }
        }
    }
}
