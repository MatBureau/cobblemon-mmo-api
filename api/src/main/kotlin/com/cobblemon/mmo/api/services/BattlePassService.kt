package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.api.middleware.ValidationException
import com.cobblemon.mmo.api.repositories.BattlePassRepository
import com.cobblemon.mmo.api.repositories.BattlePassRow
import org.slf4j.LoggerFactory

class BattlePassService(
    private val battlePassRepository: BattlePassRepository,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(BattlePassService::class.java)
    private val config = appConfig.game.battlePass

    suspend fun getProgress(playerUuid: String, seasonNumber: Int): BattlePassRow =
        battlePassRepository.getOrCreate(playerUuid, seasonNumber)

    suspend fun addXp(playerUuid: String, seasonNumber: Int, xp: Long, source: String): BattlePassRow {
        val result = battlePassRepository.addXp(playerUuid, seasonNumber, xp, config.xpPerTier, config.maxTier)
        logger.info("Added ${xp}xp from $source to player $playerUuid (season $seasonNumber), tier ${result.currentTier}")
        return result
    }

    suspend fun claimReward(playerUuid: String, seasonNumber: Int, tier: Int): Unit {
        val progress = battlePassRepository.getOrCreate(playerUuid, seasonNumber)
        if (tier > progress.currentTier)
            throw ValidationException("Tier $tier not yet reached (current: ${progress.currentTier})")
        if (tier in progress.claimedTiers)
            throw ValidationException("Tier $tier reward already claimed")
        battlePassRepository.claimTier(playerUuid, seasonNumber, tier)
        logger.info("Player $playerUuid claimed tier $tier reward (season $seasonNumber)")
    }

    suspend fun upgradeToPremium(playerUuid: String, seasonNumber: Int): Unit {
        val progress = battlePassRepository.getOrCreate(playerUuid, seasonNumber)
        if (progress.isPremium) throw ValidationException("Already a premium battle pass holder")
        battlePassRepository.setPremium(playerUuid, seasonNumber)
    }
}

class SkinService(
    private val skinRepository: SkinRepository,
    private val appConfig: AppConfig,
) {
    suspend fun getPlayerSkins(playerUuid: String): List<String> =
        skinRepository.findPlayerSkins(playerUuid)

    suspend fun applySkin(playerUuid: String, pokemonId: String, skinId: String) {
        if (!skinRepository.playerOwnsSkin(playerUuid, skinId))
            throw com.cobblemon.mmo.api.middleware.SkinNotOwnedException(playerUuid, skinId)
        // In production, update the PokemonData record with the skinId
    }

    suspend fun grantSkin(playerUuid: String, skinId: String, source: String) {
        skinRepository.grantSkin(playerUuid, skinId, source)
    }
}

class BreedingService(
    private val breedingRepository: BreedingRepository,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(BreedingService::class.java)

    suspend fun getBreedingItems(playerUuid: String) =
        breedingRepository.findAllByPlayer(playerUuid)

    suspend fun breed(
        playerUuid: String,
        parent1Id: String,
        parent2Id: String,
        breedingItemId: String,
    ): String {
        val item = breedingRepository.findByPlayerAndItem(playerUuid, breedingItemId)
            ?: throw com.cobblemon.mmo.api.middleware.BreedingItemNotFoundException(breedingItemId)

        // In production: verify player owns parent1 and parent2, check egg group compatibility
        // Consume the breeding item
        breedingRepository.decrementItem(playerUuid, breedingItemId)

        // In production: delete parents from PokemonData, apply IV inheritance rules
        // Return the child Pokemon ID
        val offspringId = java.util.UUID.randomUUID().toString()
        logger.info("Breeding completed for player $playerUuid, offspring: $offspringId")
        return offspringId
    }
}

class ChatService(
    private val playerRepository: PlayerRepository,
    private val appConfig: AppConfig,
) {
    suspend fun mutePlayer(playerUuid: String, durationMinutes: Long, reason: String) {
        val expiry = kotlinx.datetime.Clock.System.now() + kotlin.time.Duration.parse("${durationMinutes}m")
        playerRepository.mute(playerUuid, expiry)
    }

    suspend fun unmutePlayer(playerUuid: String) {
        playerRepository.unmute(playerUuid)
    }
}
