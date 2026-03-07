package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.BannerService
import com.cobblemon.mmo.common.dto.responses.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.bannerRoutes() {
    val bannerService by inject<BannerService>()

    authenticate("server-auth") {
        route("/api/banner") {
            get("/current") {
                val banner = bannerService.getCurrentBanner()
                call.respond(ApiResponse.ok(BannerResponse(
                    id = banner.id,
                    name = banner.name,
                    description = banner.description,
                    featuredLegendaryId = banner.featuredLegendaryId,
                    featuredLegendaryName = banner.featuredLegendaryName,
                    legendaryDropRate = banner.legendaryDropRate,
                    pityThreshold = banner.pityThreshold,
                    activeFrom = banner.activeFrom.toString(),
                    activeTo = banner.activeTo.toString(),
                    pullCost = banner.pullCost,
                    multiPullCost = banner.multiPullCost,
                )))
            }

            post("/pull/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val results = bannerService.pull(playerUuid, isMulti = false)
                call.respond(ApiResponse.ok(results))
            }

            post("/pull10/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val results = bannerService.pull(playerUuid, isMulti = true)
                call.respond(ApiResponse.ok(results))
            }

            get("/pity/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val banner = bannerService.getCurrentBanner()
                val (pity, threshold) = bannerService.getPity(playerUuid)
                call.respond(ApiResponse.ok(PityResponse(
                    playerUuid = playerUuid,
                    bannerId = banner.id,
                    pityCount = pity,
                    pityThreshold = threshold,
                    guaranteedIn = (threshold - pity).coerceAtLeast(1),
                )))
            }
        }
    }
}
