package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.middleware.PlayerNotFoundException
import com.cobblemon.mmo.api.repositories.PlayerRepository
import com.cobblemon.mmo.api.redis.cache.SessionCache
import com.cobblemon.mmo.common.models.Player
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

class PlayerService(
    private val playerRepository: PlayerRepository,
    private val sessionCache: SessionCache,
) {
    private val logger = LoggerFactory.getLogger(PlayerService::class.java)

    suspend fun login(uuid: String, username: String, serverName: String): Player {
        val player = playerRepository.createOrUpdate(uuid, username, serverName)
        sessionCache.setSession(uuid, serverName)
        logger.info("Player $username ($uuid) logged in to $serverName")
        return player
    }

    suspend fun logout(uuid: String): Unit {
        playerRepository.setServer(uuid, null)
        sessionCache.removeSession(uuid)
    }

    suspend fun getPlayer(uuid: String): Player =
        playerRepository.findByUuid(uuid) ?: throw PlayerNotFoundException(uuid)

    suspend fun getBalance(uuid: String): Long {
        val player = playerRepository.findByUuid(uuid) ?: throw PlayerNotFoundException(uuid)
        return player.balance
    }

    suspend fun adjustBalance(uuid: String, delta: Long): Long {
        playerRepository.findByUuid(uuid) ?: throw PlayerNotFoundException(uuid)
        return playerRepository.adjustBalance(uuid, delta)
    }

    suspend fun setBalance(uuid: String, amount: Long): Unit {
        playerRepository.findByUuid(uuid) ?: throw PlayerNotFoundException(uuid)
        playerRepository.setBalance(uuid, amount)
    }

    suspend fun getOnlineCount(): Long = sessionCache.getOnlineCount()

    suspend fun getTotalCount(): Long = playerRepository.countTotal()
}
