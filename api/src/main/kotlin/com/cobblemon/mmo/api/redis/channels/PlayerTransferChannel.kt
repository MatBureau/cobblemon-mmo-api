package com.cobblemon.mmo.api.redis.channels

import com.cobblemon.mmo.api.redis.RedisPublisher
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PlayerTransferRequest(
    val playerUuid: String,
    val targetServer: String,
    val reason: String,
)

class PlayerTransferChannel(private val publisher: RedisPublisher) {
    suspend fun requestTransfer(request: PlayerTransferRequest) {
        publisher.publish(
            "transfer:${request.targetServer}",
            Json.encodeToString(request)
        )
    }
}
