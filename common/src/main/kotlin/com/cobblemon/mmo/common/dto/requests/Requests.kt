package com.cobblemon.mmo.common.dto.requests

import kotlinx.serialization.Serializable

@Serializable
data class PlayerLoginRequest(
    val uuid: String,
    val username: String,
    val serverName: String,
)

@Serializable
data class BalanceUpdateRequest(
    val amount: Long,
    val reason: String,
)

@Serializable
data class CreateAuctionListingRequest(
    val sellerUuid: String,
    val itemType: String,
    val pokemonId: String? = null,
    val itemId: String? = null,
    val itemName: String? = null,
    val itemQuantity: Int = 1,
    val price: Long,
)

@Serializable
data class BuyAuctionRequest(
    val buyerUuid: String,
)

@Serializable
data class JoinPvpQueueRequest(
    val playerUuid: String,
)

@Serializable
data class PvpResultRequest(
    val matchId: String,
    val player1Uuid: String,
    val player2Uuid: String,
    val winnerUuid: String?,
    val durationSeconds: Int,
    val serverName: String,
)

@Serializable
data class BreedingRequest(
    val playerUuid: String,
    val parent1Id: String,
    val parent2Id: String,
    val breedingItemId: String,
)

@Serializable
data class EnterDungeonRequest(
    val playerUuid: String,
    val dungeonId: String,
    val serverName: String,
)

@Serializable
data class CompleteDungeonRequest(
    val playerUuid: String,
    val dungeonId: String,
    val sessionId: String,
)

@Serializable
data class AddBattlePassXpRequest(
    val playerUuid: String,
    val xp: Long,
    val source: String,
)

@Serializable
data class BannerPullRequest(
    val playerUuid: String,
    val bannerId: String,
)

@Serializable
data class ApplySkinRequest(
    val playerUuid: String,
    val pokemonId: String,
    val skinId: String,
)

@Serializable
data class ChatMuteRequest(
    val playerUuid: String,
    val durationMinutes: Long,
    val reason: String,
)

@Serializable
data class GiveEconomyRequest(
    val playerUuid: String,
    val balance: Long? = null,
    val itemId: String? = null,
    val quantity: Int = 1,
    val reason: String,
)
