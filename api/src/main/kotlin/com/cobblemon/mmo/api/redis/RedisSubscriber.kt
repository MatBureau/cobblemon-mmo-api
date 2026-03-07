package com.cobblemon.mmo.api.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import org.slf4j.LoggerFactory

typealias MessageHandler = suspend (channel: String, message: String) -> Unit

class RedisSubscriber(private val client: RedisClient) {
    private val logger = LoggerFactory.getLogger(RedisSubscriber::class.java)
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String> by lazy {
        client.connectPubSub()
    }

    private val handlers = mutableMapOf<String, MutableList<MessageHandler>>()

    init {
        pubSubConnection.addListener(object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                handlers[channel]?.forEach { handler ->
                    kotlinx.coroutines.GlobalScope.kotlinx.coroutines.launch {
                        try {
                            handler(channel, message)
                        } catch (e: Exception) {
                            logger.error("Error handling message on channel '$channel': ${e.message}", e)
                        }
                    }
                }
            }
        })
    }

    fun subscribe(channel: String, handler: MessageHandler) {
        handlers.getOrPut(channel) { mutableListOf() }.add(handler)
        pubSubConnection.sync().subscribe(channel)
        logger.info("Subscribed to Redis channel: $channel")
    }

    fun subscribe(channel: RedisChannel, handler: MessageHandler) =
        subscribe(channel.name, handler)

    fun unsubscribe(channel: String) {
        handlers.remove(channel)
        pubSubConnection.sync().unsubscribe(channel)
        logger.info("Unsubscribed from Redis channel: $channel")
    }

    fun close() {
        pubSubConnection.close()
    }
}
