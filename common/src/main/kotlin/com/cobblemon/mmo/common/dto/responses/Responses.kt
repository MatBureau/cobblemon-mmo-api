package com.cobblemon.mmo.common.dto.responses

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null,
) {
    companion object {
        fun <T> ok(data: T) = ApiResponse(success = true, data = data)
        fun <T> fail(code: String, message: String) = ApiResponse<T>(
            success = false,
            error = ApiError(code, message)
        )
    }
}

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)

@Serializable
data class PlayerResponse(
    val uuid: String,
    val username: String,
    val balance: Long,
    val playtimeSeconds: Long,
    val firstLogin: String,
    val lastLogin: String,
    val currentServer: String?,
)

@Serializable
data class BalanceResponse(
    val uuid: String,
    val balance: Long,
)

@Serializable
data class AuctionListingResponse(
    val id: String,
    val sellerUuid: String,
    val sellerName: String,
    val itemType: String,
    val pokemonSummary: PokemonSummaryResponse? = null,
    val itemId: String? = null,
    val itemName: String? = null,
    val itemQuantity: Int = 1,
    val rarity: String,
    val price: Long,
    val status: String,
    val createdAt: String,
    val expiresAt: String,
)

@Serializable
data class PokemonSummaryResponse(
    val id: String,
    val speciesName: String,
    val nickname: String?,
    val level: Int,
    val isShiny: Boolean,
    val ivTotal: Int,
    val nature: String,
)

@Serializable
data class PvpRankingResponse(
    val rank: Int,
    val playerUuid: String,
    val username: String,
    val elo: Int,
    val wins: Int,
    val losses: Int,
    val seasonNumber: Int,
)

@Serializable
data class PvpStatsResponse(
    val playerUuid: String,
    val elo: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val winRate: Double,
    val seasonNumber: Int,
)

@Serializable
data class QueueStatusResponse(
    val playerUuid: String,
    val elo: Int,
    val queuedAt: String,
    val waitSeconds: Long,
)

@Serializable
data class MatchFoundResponse(
    val matchId: String,
    val opponent1Uuid: String,
    val opponent2Uuid: String,
    val serverName: String,
)

@Serializable
data class ResinResponse(
    val playerUuid: String,
    val current: Int,
    val max: Int,
    val minutesToNextResin: Long,
)

@Serializable
data class DungeonLootResponse(
    val sessionId: String,
    val loot: List<LootItemResponse>,
)

@Serializable
data class LootItemResponse(
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val rarity: String,
)

@Serializable
data class BattlePassResponse(
    val playerUuid: String,
    val seasonNumber: Int,
    val currentTier: Int,
    val currentXp: Long,
    val xpToNextTier: Long,
    val isPremium: Boolean,
    val claimedTiers: List<Int>,
)

@Serializable
data class BannerResponse(
    val id: String,
    val name: String,
    val description: String,
    val featuredLegendaryId: String,
    val featuredLegendaryName: String,
    val legendaryDropRate: Double,
    val pityThreshold: Int,
    val activeFrom: String,
    val activeTo: String,
    val pullCost: Long,
    val multiPullCost: Long,
)

@Serializable
data class PityResponse(
    val playerUuid: String,
    val bannerId: String,
    val pityCount: Int,
    val pityThreshold: Int,
    val guaranteedIn: Int,
)

@Serializable
data class SkinResponse(
    val id: String,
    val name: String,
    val description: String,
    val speciesId: String,
    val speciesName: String,
    val rarity: String,
    val isLimitedTime: Boolean,
    val availableUntil: String? = null,
)

@Serializable
data class GlobalStatsResponse(
    val onlinePlayers: Int,
    val totalPlayers: Long,
    val activeAuctionListings: Long,
    val totalAuctionVolume: Long,
    val currentSeason: Int,
    val activeBannerId: String?,
)
