package com.cobblemon.mmo.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Resin(
    val playerUuid: String,
    val current: Int,
    val max: Int = 160,
    val lastRegenAt: Instant,
    val updatedAt: Instant,
) {
    fun resinSince(now: Instant, regenIntervalMinutes: Int): Int {
        val minutesElapsed = (now - lastRegenAt).inWholeMinutes
        return (minutesElapsed / regenIntervalMinutes).toInt().coerceAtMost(max - current)
    }

    fun computedCurrent(now: Instant, regenIntervalMinutes: Int): Int =
        (current + resinSince(now, regenIntervalMinutes)).coerceAtMost(max)
}
