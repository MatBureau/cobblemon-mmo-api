package com.cobblemon.mmo.api.redis.channels

import com.cobblemon.mmo.api.redis.RedisChannel
import com.cobblemon.mmo.api.redis.RedisPublisher
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class GlobalEvent(
    val type: String,  // "BANNER_ROTATED", "SEASON_ENDED", "ANNOUNCEMENT"
    val payload: String, // JSON-serialized event data
    val timestamp: Long = System.currentTimeMillis(),
)

class EventChannel(private val publisher: RedisPublisher) {
    suspend fun broadcast(event: GlobalEvent) {
        publisher.publish(RedisChannel.EVENT_BROADCAST.name, Json.encodeToString(event))
    }

    suspend fun broadcastAnnouncement(message: String) {
        broadcast(GlobalEvent(type = "ANNOUNCEMENT", payload = Json.encodeToString(mapOf("message" to message))))
    }

    suspend fun broadcastBannerRotated(bannerId: String, bannerName: String) {
        broadcast(GlobalEvent(
            type = "BANNER_ROTATED",
            payload = Json.encodeToString(mapOf("id" to bannerId, "name" to bannerName))
        ))
    }

    suspend fun broadcastSeasonEnded(season: Int) {
        broadcast(GlobalEvent(
            type = "SEASON_ENDED",
            payload = Json.encodeToString(mapOf("season" to season.toString()))
        ))
    }
}
