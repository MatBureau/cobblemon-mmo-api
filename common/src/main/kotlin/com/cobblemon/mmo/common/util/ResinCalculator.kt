package com.cobblemon.mmo.common.util

import kotlinx.datetime.Instant

/**
 * Utility for computing Resin state based on time elapsed.
 * Keeps models pure and calculations testable.
 */
object ResinCalculator {

    /**
     * Computes how much resin has regenerated since [lastRegenAt].
     *
     * @param current Current resin amount
     * @param max Maximum resin cap
     * @param lastRegenAt Timestamp of last regen tick
     * @param now Current time
     * @param regenIntervalMinutes Minutes per 1 resin
     * @return Clamped current resin
     */
    fun computeCurrent(
        current: Int,
        max: Int,
        lastRegenAt: Instant,
        now: Instant,
        regenIntervalMinutes: Int = 8,
    ): Int {
        if (current >= max) return max
        val minutesElapsed = (now - lastRegenAt).inWholeMinutes
        val regenAmount = (minutesElapsed / regenIntervalMinutes).toInt()
        return (current + regenAmount).coerceAtMost(max)
    }

    /**
     * Returns the number of minutes until the next resin tick.
     */
    fun minutesToNextRegen(
        lastRegenAt: Instant,
        now: Instant,
        regenIntervalMinutes: Int = 8,
    ): Long {
        val minutesElapsed = (now - lastRegenAt).inWholeMinutes % regenIntervalMinutes
        return regenIntervalMinutes - minutesElapsed
    }

    /**
     * Returns the time until resin is full.
     */
    fun minutesToFull(
        current: Int,
        max: Int,
        lastRegenAt: Instant,
        now: Instant,
        regenIntervalMinutes: Int = 8,
    ): Long {
        val computedCurrent = computeCurrent(current, max, lastRegenAt, now, regenIntervalMinutes)
        if (computedCurrent >= max) return 0L
        val remaining = max - computedCurrent
        val toNextTick = minutesToNextRegen(lastRegenAt, now, regenIntervalMinutes)
        return toNextTick + (remaining - 1).toLong() * regenIntervalMinutes
    }
}
