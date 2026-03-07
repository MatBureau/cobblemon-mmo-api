package com.cobblemon.mmo.api.redis.channels

import com.cobblemon.mmo.api.redis.RedisChannel
import com.cobblemon.mmo.api.redis.RedisPublisher
import com.cobblemon.mmo.api.redis.RedisSubscriber
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class MatchFoundEvent(
    val matchId: String,
    val player1Uuid: String,
    val player2Uuid: String,
    val serverName: String,
)

class MatchmakingChannel(
    private val publisher: RedisPublisher,
    private val subscriber: RedisSubscriber,
) {
    private val logger = LoggerFactory.getLogger(MatchmakingChannel::class.java)

    suspend fun publishMatchFound(event: MatchFoundEvent) {
        publisher.publish(RedisChannel.MATCHMAKING_FOUND.name, Json.encodeToString(event))
        logger.info("Published match found: ${event.matchId} — ${event.player1Uuid} vs ${event.player2Uuid}")
    }

    fun onMatchFound(handler: suspend (MatchFoundEvent) -> Unit) {
        subscriber.subscribe(RedisChannel.MATCHMAKING_FOUND.name) { _, message ->
            try {
                handler(Json.decodeFromString(message))
            } catch (e: Exception) {
                logger.error("Failed to decode match found event: ${e.message}")
            }
        }
    }
}
