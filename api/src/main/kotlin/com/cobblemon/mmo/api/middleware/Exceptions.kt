package com.cobblemon.mmo.api.middleware

/**
 * Business logic exceptions — each is mapped to an HTTP status + error code in StatusPages.
 */

class PlayerNotFoundException(uuid: String) :
    RuntimeException("Player not found: $uuid")

class InsufficientBalanceException(required: Long, actual: Long) :
    RuntimeException("Insufficient balance: required $required, have $actual")

class InsufficientResinException(required: Int, actual: Int) :
    RuntimeException("Insufficient resin: required $required, have $actual")

class InvalidBreedingException(reason: String) :
    RuntimeException("Invalid breeding: $reason")

class AuctionExpiredException(id: String) :
    RuntimeException("Auction listing expired or not found: $id")

class AuctionNotFoundException(id: String) :
    RuntimeException("Auction listing not found: $id")

class AlreadyInQueueException(playerUuid: String) :
    RuntimeException("Player $playerUuid is already in the matchmaking queue")

class NotInQueueException(playerUuid: String) :
    RuntimeException("Player $playerUuid is not in the matchmaking queue")

class BannerNotFoundException(id: String) :
    RuntimeException("Banner not found: $id")

class NoBannerActiveException :
    RuntimeException("No banner is currently active")

class SkinNotFoundException(id: String) :
    RuntimeException("Skin not found: $id")

class SkinNotOwnedException(playerUuid: String, skinId: String) :
    RuntimeException("Player $playerUuid does not own skin $skinId")

class PokemonNotFoundException(id: String) :
    RuntimeException("Pokemon not found: $id")

class PokemonNotOwnedException(playerUuid: String, pokemonId: String) :
    RuntimeException("Player $playerUuid does not own pokemon $pokemonId")

class BreedingItemNotFoundException(itemId: String) :
    RuntimeException("Breeding item not found or not owned: $itemId")

class IncompatibleEggGroupsException(species1: String, species2: String) :
    RuntimeException("Incompatible egg groups for $species1 and $species2")

class SeasonNotFoundException(season: Int) :
    RuntimeException("Season $season not found")

class UnauthorizedException(message: String = "Unauthorized") :
    RuntimeException(message)

class ValidationException(message: String) :
    RuntimeException(message)
