package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.api.middleware.InsufficientResinException
import com.cobblemon.mmo.api.middleware.PlayerNotFoundException
import com.cobblemon.mmo.api.repositories.ResinRepository
import com.cobblemon.mmo.api.repositories.PlayerRepository
import com.cobblemon.mmo.common.util.ResinCalculator
import com.cobblemon.mmo.common.dto.responses.LootItemResponse
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.random.Random

class ResinService(
    private val resinRepository: ResinRepository,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(ResinService::class.java)
    private val config = appConfig.game.resin

    suspend fun getResin(playerUuid: String): Triple<Int, Int, Long> {
        val row = resinRepository.getOrCreate(playerUuid, config.maxResin)
        val now = Clock.System.now()
        val computed = ResinCalculator.computeCurrent(row.current, row.max, row.lastRegenAt, now, config.regenIntervalMinutes)
        val minutesToNext = ResinCalculator.minutesToNextRegen(row.lastRegenAt, now, config.regenIntervalMinutes)
        return Triple(computed, row.max, minutesToNext)
    }

    suspend fun consumeResin(playerUuid: String, amount: Int): Int {
        val row = resinRepository.getOrCreate(playerUuid, config.maxResin)
        val now = Clock.System.now()
        val computed = ResinCalculator.computeCurrent(row.current, row.max, row.lastRegenAt, now, config.regenIntervalMinutes)
        if (computed < amount) throw InsufficientResinException(required = amount, actual = computed)
        val newAmount = resinRepository.consumeResin(playerUuid, amount, computed)
        logger.info("Player $playerUuid consumed $amount resin (had $computed, now $newAmount)")
        return newAmount
    }

    suspend fun addResin(playerUuid: String, amount: Int) {
        val row = resinRepository.getOrCreate(playerUuid, config.maxResin)
        val now = Clock.System.now()
        val computed = ResinCalculator.computeCurrent(row.current, row.max, row.lastRegenAt, now, config.regenIntervalMinutes)
        val newAmount = (computed + amount).coerceAtMost(config.maxResin)
        resinRepository.setResin(playerUuid, newAmount)
    }
}

class DungeonService(
    private val resinService: ResinService,
    private val appConfig: AppConfig,
) {
    private val DUNGEON_COST = appConfig.game.resin.dungeonCost
    private val logger = LoggerFactory.getLogger(DungeonService::class.java)

    /** Generates a loot table roll for a completed dungeon */
    fun rollLoot(): List<LootItemResponse> {
        val rolls = mutableListOf<LootItemResponse>()
        val roll = Random.nextDouble()

        val loot = when {
            roll < 0.001 -> LootItemResponse("master_ball", "Master Ball", 1, "LEGENDARY")
            roll < 0.011 -> LootItemResponse("talent_pill", "Hidden Ability Pill", 1, "EPIC")
            roll < 0.061 -> LootItemResponse("rare_candy_rare", "Rare Candy (Rare)", 3, "RARE")
            roll < 0.161 -> LootItemResponse("protein", "Protein", 2, "UNCOMMON")
            roll < 0.461 -> LootItemResponse("rare_candy_common", "Rare Candy", 5, "COMMON")
            else -> LootItemResponse("potion", "Potion", 5, "COMMON")
        }

        rolls.add(loot)
        // Add 1-2 bonus common drops
        repeat(Random.nextInt(1, 3)) {
            rolls.add(LootItemResponse("berry", "Berry", Random.nextInt(1, 5), "COMMON"))
        }
        return rolls
    }

    suspend fun enterDungeon(playerUuid: String, dungeonId: String): Int =
        resinService.consumeResin(playerUuid, DUNGEON_COST)

    suspend fun completeDungeon(playerUuid: String, sessionId: String): List<LootItemResponse> {
        return rollLoot().also {
            logger.info("Player $playerUuid completed dungeon $sessionId, loot: ${it.map { l -> l.itemId }}")
        }
    }
}
