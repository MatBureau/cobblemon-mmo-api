package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.api.middleware.AlreadyInQueueException
import com.cobblemon.mmo.api.middleware.NotInQueueException
import com.cobblemon.mmo.api.redis.RedisPublisher
import com.cobblemon.mmo.api.redis.channels.MatchFoundEvent
import com.cobblemon.mmo.api.repositories.PvpMatchRow
import com.cobblemon.mmo.api.repositories.PvpRepository
import com.cobblemon.mmo.common.util.EloCalculator
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class QueueEntry(val playerUuid: String, val elo: Int, val queuedAt: Long = System.currentTimeMillis())

class MatchmakingService(
    private val pvpRepository: PvpRepository,
    private val redis: RedisAsyncCommands<String, String>,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(MatchmakingService::class.java)
    private val queueKey = "pvp:queue"
    private val queueMetaPrefix = "pvp:meta:"

    suspend fun joinQueue(playerUuid: String, elo: Int) {
        val existing = redis.zscore(queueKey, playerUuid).await()
        if (existing != null) throw AlreadyInQueueException(playerUuid)

        redis.zadd(queueKey, elo.toDouble(), playerUuid).await()
        redis.setex("${queueMetaPrefix}$playerUuid", 300, Json.encodeToString(QueueEntry(playerUuid, elo))).await()
        logger.info("Player $playerUuid (ELO $elo) joined matchmaking queue")
    }

    suspend fun leaveQueue(playerUuid: String) {
        val removed = redis.zrem(queueKey, playerUuid).await()
        redis.del("${queueMetaPrefix}$playerUuid").await()
        if (removed == 0L) throw NotInQueueException(playerUuid)
        logger.info("Player $playerUuid left matchmaking queue")
    }

    suspend fun isInQueue(playerUuid: String): Boolean =
        redis.zscore(queueKey, playerUuid).await() != null

    /**
     * Scans the queue and matches players with close ELO ratings.
     * Called every 5 seconds by the scheduler.
     */
    suspend fun processQueue() {
        val allEntries = redis.zrangeWithScores(queueKey, 0, -1).await() ?: return
        if (allEntries.size < 2) return

        val matched = mutableSetOf<String>()

        for (entry in allEntries) {
            val playerUuid = entry.value
            if (playerUuid in matched) continue

            val playerElo = entry.score.toInt()
            val queuedAt = redis.get("${queueMetaPrefix}$playerUuid").await()
                ?.let { Json.decodeFromString<QueueEntry>(it).queuedAt } ?: continue

            val waitSeconds = (System.currentTimeMillis() - queuedAt) / 1000
            val eloRange = when {
                waitSeconds > 60 -> 500
                waitSeconds > 30 -> 200
                else -> 100
            }

            val opponent = allEntries.firstOrNull { other ->
                other.value != playerUuid &&
                        other.value !in matched &&
                        kotlin.math.abs(other.score - playerElo) <= eloRange
            } ?: continue

            // Match found!
            matched.add(playerUuid)
            matched.add(opponent.value)

            redis.zrem(queueKey, playerUuid, opponent.value).await()
            redis.del("${queueMetaPrefix}$playerUuid", "${queueMetaPrefix}${opponent.value}").await()

            val event = MatchFoundEvent(
                matchId = UUID.randomUUID().toString(),
                player1Uuid = playerUuid,
                player2Uuid = opponent.value,
                serverName = "pvp-1", // In production, pick from available PvP servers
            )
            redis.publish("matchmaking:found", Json.encodeToString(event)).await()
            logger.info("Match found: ${event.matchId} — $playerUuid vs ${opponent.value}")
        }
    }
}

class EloService(
    private val pvpRepository: PvpRepository,
    private val appConfig: AppConfig,
) {
    private val logger = LoggerFactory.getLogger(EloService::class.java)

    suspend fun recordMatchResult(
        player1Uuid: String,
        player2Uuid: String,
        winnerUuid: String?,
        seasonNumber: Int,
        durationSeconds: Int,
        serverName: String,
    ) {
        val kFactor = appConfig.game.pvp.kFactor
        val defaultElo = appConfig.game.pvp.defaultElo

        val rank1 = pvpRepository.getOrCreateRanking(player1Uuid, seasonNumber, defaultElo)
        val rank2 = pvpRepository.getOrCreateRanking(player2Uuid, seasonNumber, defaultElo)

        val isDraw = winnerUuid == null
        val (elo1After, elo2After) = if (isDraw) {
            EloCalculator.calculate(rank1.elo, rank2.elo, kFactor, isDraw = true)
        } else if (winnerUuid == player1Uuid) {
            EloCalculator.calculate(rank1.elo, rank2.elo, kFactor)
        } else {
            val (w, l) = EloCalculator.calculate(rank2.elo, rank1.elo, kFactor)
            Pair(l, w) // swap back to (p1, p2)
        }

        pvpRepository.updateRanking(player1Uuid, seasonNumber, elo1After,
            isWin = winnerUuid == player1Uuid, isDraw = isDraw)
        pvpRepository.updateRanking(player2Uuid, seasonNumber, elo2After,
            isWin = winnerUuid == player2Uuid, isDraw = isDraw)

        pvpRepository.recordMatch(PvpMatchRow(
            id = UUID.randomUUID().toString(),
            seasonNumber = seasonNumber,
            player1Uuid = player1Uuid,
            player2Uuid = player2Uuid,
            winnerUuid = winnerUuid,
            player1EloBefore = rank1.elo,
            player2EloBefore = rank2.elo,
            player1EloAfter = elo1After,
            player2EloAfter = elo2After,
            serverName = serverName,
            playedAt = Clock.System.now(),
            durationSeconds = durationSeconds,
        ))

        logger.info("Match result recorded: $player1Uuid ($elo1After) vs $player2Uuid ($elo2After), winner: $winnerUuid")
    }
}
