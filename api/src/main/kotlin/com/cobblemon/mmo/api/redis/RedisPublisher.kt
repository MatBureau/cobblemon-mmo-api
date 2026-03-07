package com.cobblemon.mmo.api.redis

import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory

class RedisPublisher(
    private val commands: RedisAsyncCommands<String, String>,
) {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    /**
     * Publishes [message] to the given [channel].
     * @return number of subscribers that received the message
     */
    suspend fun publish(channel: String, message: String): Long {
        logger.debug("Publishing to channel='{}': {}", channel, message)
        return commands.publish(channel, message).await()
    }

    /** Convenience publish for typed channels */
    suspend fun publish(channel: RedisChannel, message: String): Long =
        publish(channel.name, message)
}

/** Typed Redis channel registry */
enum class RedisChannel(val name: String) {
    CHAT_GLOBAL("chat:global"),
    CHAT_TRADE("chat:trade"),
    MATCHMAKING_QUEUE("matchmaking:queue"),
    MATCHMAKING_FOUND("matchmaking:found"),
    EVENT_BROADCAST("event:broadcast");

    companion object {
        fun chatRegion(serverId: String) = "chat:region:$serverId"
        fun chatGuild(guildId: String) = "chat:guild:$guildId"
        fun chatWhisper(targetUuid: String) = "chat:whisper:$targetUuid"
        fun transfer(serverName: String) = "transfer:$serverName"
        fun session(playerUuid: String) = "session:$playerUuid"
    }
}
