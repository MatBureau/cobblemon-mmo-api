package com.cobblemon.mmo.api.redis.cache

import com.cobblemon.mmo.common.models.AuctionListing
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class AuctionCache(private val redis: RedisAsyncCommands<String, String>) {
    private val logger = LoggerFactory.getLogger(AuctionCache::class.java)
    private val listingsKey = "auction:active"
    private val ttlSeconds = 300L // 5 minutes

    suspend fun cacheListings(listings: List<AuctionListing>) {
        val serialized = Json.encodeToString(listings)
        redis.setex(listingsKey, ttlSeconds, serialized).await()
    }

    suspend fun getCachedListings(): List<AuctionListing>? {
        val value = redis.get(listingsKey).await() ?: return null
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            logger.warn("Failed to decode cached auction listings: ${e.message}")
            null
        }
    }

    suspend fun invalidate() {
        redis.del(listingsKey).await()
    }
}

class SessionCache(private val redis: RedisAsyncCommands<String, String>) {
    private val ttlSeconds = 1800L // 30 minutes

    suspend fun setSession(playerUuid: String, serverName: String) {
        redis.setex("session:$playerUuid", ttlSeconds, serverName).await()
    }

    suspend fun getSession(playerUuid: String): String? =
        redis.get("session:$playerUuid").await()

    suspend fun refreshSession(playerUuid: String) {
        redis.expire("session:$playerUuid", ttlSeconds).await()
    }

    suspend fun removeSession(playerUuid: String) {
        redis.del("session:$playerUuid").await()
    }

    suspend fun getOnlineCount(): Long =
        redis.keys("session:*").await()?.size?.toLong() ?: 0L
}

class ResinCache(private val redis: RedisAsyncCommands<String, String>) {
    private val ttlSeconds = 600L // 10 minutes

    suspend fun setResin(playerUuid: String, current: Int) {
        redis.setex("resin:$playerUuid", ttlSeconds, current.toString()).await()
    }

    suspend fun getResin(playerUuid: String): Int? =
        redis.get("resin:$playerUuid").await()?.toIntOrNull()

    suspend fun invalidate(playerUuid: String) {
        redis.del("resin:$playerUuid").await()
    }
}
