package com.cobblemon.mmo.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BattlePassReward(
    val tier: Int,
    val isPremium: Boolean,
    val itemId: String,
    val itemName: String,
    val quantity: Int,
)

@Serializable
data class BattlePassProgress(
    val playerUuid: String,
    val seasonNumber: Int,
    val currentTier: Int,
    val currentXp: Long,
    val xpToNextTier: Long,
    val isPremium: Boolean,
    val claimedTiers: List<Int>,
    val updatedAt: Instant,
)
