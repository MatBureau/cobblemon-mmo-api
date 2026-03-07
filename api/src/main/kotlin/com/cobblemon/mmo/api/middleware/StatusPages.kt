package com.cobblemon.mmo.api.middleware

import com.cobblemon.mmo.common.dto.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        // 400 Bad Request
        exception<ValidationException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("VALIDATION_ERROR", e.message ?: "Validation error"))
        }
        exception<InvalidBreedingException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("INVALID_BREEDING", e.message ?: "Invalid breeding"))
        }
        exception<IncompatibleEggGroupsException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse.fail<Unit>("INCOMPATIBLE_EGG_GROUPS", e.message ?: "Incompatible egg groups"))
        }

        // 401 Unauthorized
        exception<UnauthorizedException> { call, e ->
            call.respond(HttpStatusCode.Unauthorized, ApiResponse.fail<Unit>("UNAUTHORIZED", e.message ?: "Unauthorized"))
        }

        // 404 Not Found
        exception<PlayerNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("PLAYER_NOT_FOUND", e.message ?: "Player not found"))
        }
        exception<AuctionNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("AUCTION_NOT_FOUND", e.message ?: "Auction not found"))
        }
        exception<BannerNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("BANNER_NOT_FOUND", e.message ?: "Banner not found"))
        }
        exception<NoBannerActiveException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("NO_BANNER_ACTIVE", e.message ?: "No active banner"))
        }
        exception<SkinNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("SKIN_NOT_FOUND", e.message ?: "Skin not found"))
        }
        exception<PokemonNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("POKEMON_NOT_FOUND", e.message ?: "Pokemon not found"))
        }
        exception<BreedingItemNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("BREEDING_ITEM_NOT_FOUND", e.message ?: "Breeding item not found"))
        }
        exception<SeasonNotFoundException> { call, e ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.fail<Unit>("SEASON_NOT_FOUND", e.message ?: "Season not found"))
        }

        // 409 Conflict
        exception<AlreadyInQueueException> { call, e ->
            call.respond(HttpStatusCode.Conflict, ApiResponse.fail<Unit>("ALREADY_IN_QUEUE", e.message ?: "Already in queue"))
        }
        exception<AuctionExpiredException> { call, e ->
            call.respond(HttpStatusCode.Gone, ApiResponse.fail<Unit>("AUCTION_EXPIRED", e.message ?: "Auction expired"))
        }

        // 422 Unprocessable Entity
        exception<InsufficientBalanceException> { call, e ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.fail<Unit>("INSUFFICIENT_BALANCE", e.message ?: "Insufficient balance"))
        }
        exception<InsufficientResinException> { call, e ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.fail<Unit>("INSUFFICIENT_RESIN", e.message ?: "Insufficient resin"))
        }
        exception<SkinNotOwnedException> { call, e ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.fail<Unit>("SKIN_NOT_OWNED", e.message ?: "Skin not owned"))
        }
        exception<PokemonNotOwnedException> { call, e ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.fail<Unit>("POKEMON_NOT_OWNED", e.message ?: "Pokemon not owned"))
        }
        exception<NotInQueueException> { call, e ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.fail<Unit>("NOT_IN_QUEUE", e.message ?: "Not in queue"))
        }

        // 500 Internal Server Error (catch-all)
        exception<Throwable> { call, e ->
            logger.error("Unhandled exception: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse.fail<Unit>("INTERNAL_ERROR", "An internal error occurred"))
        }
    }
}
