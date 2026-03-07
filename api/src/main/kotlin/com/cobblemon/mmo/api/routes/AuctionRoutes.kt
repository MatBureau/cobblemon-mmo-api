package com.cobblemon.mmo.api.routes

import com.cobblemon.mmo.api.services.AuctionService
import com.cobblemon.mmo.common.dto.requests.BuyAuctionRequest
import com.cobblemon.mmo.common.dto.requests.CreateAuctionListingRequest
import com.cobblemon.mmo.common.dto.responses.ApiResponse
import com.cobblemon.mmo.common.dto.responses.AuctionListingResponse
import com.cobblemon.mmo.common.dto.responses.PokemonSummaryResponse
import com.cobblemon.mmo.common.models.AuctionListing
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.auctionRoutes() {
    val auctionService by inject<AuctionService>()

    authenticate("server-auth") {
        route("/api/auction") {

            // POST /api/auction/list — Create a listing
            post("/list") {
                val req = call.receive<CreateAuctionListingRequest>()
                val listing = auctionService.createListing(
                    sellerUuid = req.sellerUuid,
                    itemType = req.itemType,
                    pokemon = null, // In production, resolve from PokemonService
                    itemId = req.itemId,
                    itemName = req.itemName,
                    itemQuantity = req.itemQuantity,
                    price = req.price,
                )
                call.respond(HttpStatusCode.Created, ApiResponse.ok(listing.toResponse()))
            }

            // GET /api/auction/listings — List active with optional filters
            get("/listings") {
                val itemType = call.request.queryParameters["itemType"]
                val minPrice = call.request.queryParameters["minPrice"]?.toLongOrNull()
                val maxPrice = call.request.queryParameters["maxPrice"]?.toLongOrNull()
                val rarity = call.request.queryParameters["rarity"]
                val speciesName = call.request.queryParameters["species"]

                val listings = auctionService.getActiveListings(itemType, minPrice, maxPrice, rarity, speciesName)
                call.respond(ApiResponse.ok(listings.map { it.toResponse() }))
            }

            // POST /api/auction/{id}/buy — Buy a listing
            post("/{id}/buy") {
                val id = call.parameters["id"]!!
                val req = call.receive<BuyAuctionRequest>()
                val result = auctionService.buyListing(id, req.buyerUuid)
                call.respond(ApiResponse.ok(result.toResponse()))
            }

            // POST /api/auction/{id}/cancel — Cancel a listing
            post("/{id}/cancel") {
                val id = call.parameters["id"]!!
                val sellerUuid = call.request.headers["X-Player-UUID"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("MISSING_HEADER", "X-Player-UUID header required"))
                val result = auctionService.cancelListing(id, sellerUuid)
                call.respond(ApiResponse.ok(result.toResponse()))
            }

            // GET /api/auction/history/{playerUuid} — Player auction history
            get("/history/{playerUuid}") {
                val playerUuid = call.parameters["playerUuid"]!!
                val history = auctionService.getHistory(playerUuid)
                call.respond(ApiResponse.ok(history.map { it.toResponse() }))
            }
        }
    }
}

private fun AuctionListing.toResponse(): AuctionListingResponse = AuctionListingResponse(
    id = id,
    sellerUuid = sellerUuid,
    sellerName = sellerName,
    itemType = itemType,
    pokemonSummary = pokemon?.let {
        PokemonSummaryResponse(
            id = it.id,
            speciesName = it.speciesName,
            nickname = it.nickname,
            level = it.level,
            isShiny = it.isShiny,
            ivTotal = it.ivs.total,
            nature = it.nature.name,
        )
    },
    itemId = itemId,
    itemName = itemName,
    itemQuantity = itemQuantity,
    rarity = rarity.name,
    price = price,
    status = status.name,
    createdAt = createdAt.toString(),
    expiresAt = expiresAt.toString(),
)
