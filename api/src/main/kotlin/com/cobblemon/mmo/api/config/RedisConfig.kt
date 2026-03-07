package com.cobblemon.mmo.api.config

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.ktor.server.config.*

object RedisConfig {
    fun createClient(config: ApplicationConfig): RedisClient {
        val url = config.property("redis.url").getString()
        return RedisClient.create(url)
    }

    fun createConnection(client: RedisClient): StatefulRedisConnection<String, String> =
        client.connect()
}
