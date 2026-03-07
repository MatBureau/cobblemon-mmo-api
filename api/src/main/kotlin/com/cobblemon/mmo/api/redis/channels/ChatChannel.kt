package com.cobblemon.mmo.api.redis.channels

import com.cobblemon.mmo.api.redis.RedisChannel
import com.cobblemon.mmo.api.redis.RedisPublisher
import com.cobblemon.mmo.api.redis.RedisSubscriber
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

@Serializable
data class ChatMessage(
    val senderUuid: String,
    val senderName: String,
    val content: String,
    val channel: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class ChatChannel(
    private val publisher: RedisPublisher,
    private val subscriber: RedisSubscriber,
) {
    private val logger = LoggerFactory.getLogger(ChatChannel::class.java)

    /** Sends a message to the global chat channel */
    suspend fun sendGlobal(message: ChatMessage) {
        publisher.publish(RedisChannel.CHAT_GLOBAL.name, Json.encodeToString(message))
    }

    /** Sends a message to the trade chat channel */
    suspend fun sendTrade(message: ChatMessage) {
        publisher.publish(RedisChannel.CHAT_TRADE.name, Json.encodeToString(message))
    }

    /** Sends a message to a region-specific channel */
    suspend fun sendRegion(serverId: String, message: ChatMessage) {
        publisher.publish(RedisChannel.chatRegion(serverId), Json.encodeToString(message))
    }

    /** Sends a whisper to a specific player */
    suspend fun sendWhisper(targetUuid: String, message: ChatMessage) {
        publisher.publish(RedisChannel.chatWhisper(targetUuid), Json.encodeToString(message))
    }

    /** Subscribe to global chat messages (e.g. for logging / moderation) */
    fun subscribeGlobal(handler: suspend (ChatMessage) -> Unit) {
        subscriber.subscribe(RedisChannel.CHAT_GLOBAL.name) { _, message ->
            try {
                handler(Json.decodeFromString(message))
            } catch (e: Exception) {
                logger.error("Failed to decode chat message: ${e.message}")
            }
        }
    }
}
