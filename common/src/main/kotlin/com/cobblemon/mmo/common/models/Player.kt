package com.cobblemon.mmo.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val uuid: String,
    val username: String,
    val balance: Long = 0L,
    val playtimeSeconds: Long = 0L,
    val firstLogin: Instant,
    val lastLogin: Instant,
    val currentServer: String? = null,
    val isMuted: Boolean = false,
    val muteExpiry: Instant? = null,
    val isBanned: Boolean = false,
)
