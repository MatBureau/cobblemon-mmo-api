package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.api.middleware.NoBannerActiveException
import com.cobblemon.mmo.api.middleware.InsufficientBalanceException
import com.cobblemon.mmo.api.middleware.PlayerNotFoundException
import com.cobblemon.mmo.api.repositories.BannerRepository
import com.cobblemon.mmo.api.repositories.BannerRow
import com.cobblemon.mmo.api.repositories.PlayerRepository
import com.cobblemon.mmo.common.enums.Rarity
import com.cobblemon.mmo.common.models.BannerPull
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.random.Random

class BannerService(
    private val bannerRepository: BannerRepository,
    private val playerRepository: PlayerRepository,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(BannerService::class.java)

    suspend fun getCurrentBanner(): BannerRow =
        bannerRepository.findActive() ?: throw NoBannerActiveException()

    suspend fun getPity(playerUuid: String): Pair<Int, Int> {
        val banner = getCurrentBanner()
        val pity = bannerRepository.getPity(playerUuid, banner.id)
        return Pair(pity, banner.pityThreshold)
    }

    suspend fun pull(playerUuid: String, isMulti: Boolean = false): List<BannerPull> {
        val banner = getCurrentBanner()
        val player = playerRepository.findByUuid(playerUuid) ?: throw PlayerNotFoundException(playerUuid)
        val cost = if (isMulti) banner.multiPullCost else banner.pullCost
        val pulls = if (isMulti) 10 else 1

        if (player.balance < cost)
            throw InsufficientBalanceException(required = cost, actual = player.balance)

        playerRepository.adjustBalance(playerUuid, -cost)

        var pity = bannerRepository.getPity(playerUuid, banner.id)
        val results = mutableListOf<BannerPull>()

        repeat(pulls) {
            pity++
            val isGuaranteed = pity >= banner.pityThreshold
            val isLegendary = isGuaranteed || Random.nextDouble() < banner.legendaryDropRate

            val (speciesId, speciesName, rarity) = if (isLegendary) {
                Triple(banner.featuredLegendaryId, banner.featuredLegendaryName, Rarity.LEGENDARY)
            } else {
                // Roll non-legendary rarity
                val r = Random.nextDouble()
                when {
                    r < 0.04 -> Triple("epic-pokemon-${UUID.randomUUID()}", "Epic Pokemon", Rarity.EPIC)
                    r < 0.15 -> Triple("rare-pokemon-${UUID.randomUUID()}", "Rare Pokemon", Rarity.RARE)
                    r < 0.35 -> Triple("uncommon-pokemon-${UUID.randomUUID()}", "Uncommon Pokemon", Rarity.UNCOMMON)
                    else -> Triple("common-pokemon-${UUID.randomUUID()}", "Common Pokemon", Rarity.COMMON)
                }
            }

            val wasPity = isGuaranteed
            if (isLegendary) pity = 0

            val pull = BannerPull(
                id = UUID.randomUUID().toString(),
                playerUuid = playerUuid,
                bannerId = banner.id,
                bannerName = banner.name,
                resultSpeciesId = speciesId,
                resultSpeciesName = speciesName,
                resultRarity = rarity,
                isLegendary = isLegendary,
                wasPity = wasPity,
                pityCountAtPull = pity,
                pulledAt = Clock.System.now(),
            )
            results.add(pull)
            bannerRepository.recordPull(pull)
        }

        bannerRepository.updatePity(playerUuid, banner.id, pity)
        logger.info("Player $playerUuid made ${pulls} pull(s) on banner ${banner.name}, pity now $pity")
        return results
    }
}
