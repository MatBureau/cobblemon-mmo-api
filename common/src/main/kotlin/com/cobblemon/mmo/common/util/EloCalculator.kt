package com.cobblemon.mmo.common.util

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Standard ELO rating calculator using the K-factor system.
 */
object EloCalculator {

    /**
     * Calculates the expected win probability for [ratingA] against [ratingB].
     */
    fun expectedScore(ratingA: Int, ratingB: Int): Double =
        1.0 / (1.0 + 10.0.pow((ratingB - ratingA) / 400.0))

    /**
     * Calculates new ELO ratings after a match.
     *
     * @param winnerElo Elo of the winner
     * @param loserElo Elo of the loser
     * @param kFactor Rating volatility constant (default 32)
     * @param isDraw Whether the match was a draw
     * @return Pair of (newWinnerElo, newLoserElo)
     */
    fun calculate(
        winnerElo: Int,
        loserElo: Int,
        kFactor: Int = 32,
        isDraw: Boolean = false,
    ): Pair<Int, Int> {
        val expectedWinner = expectedScore(winnerElo, loserElo)
        val expectedLoser = 1.0 - expectedWinner

        val actualWinner = if (isDraw) 0.5 else 1.0
        val actualLoser = if (isDraw) 0.5 else 0.0

        val newWinnerElo = (winnerElo + kFactor * (actualWinner - expectedWinner)).roundToInt()
        val newLoserElo = (loserElo + kFactor * (actualLoser - expectedLoser)).roundToInt()

        return Pair(newWinnerElo.coerceAtLeast(0), newLoserElo.coerceAtLeast(0))
    }

    /**
     * Soft reset ELO for a season reset.
     * New ELO = (currentElo + BASE_ELO) / 2
     */
    fun softReset(currentElo: Int, baseElo: Int = 1000): Int =
        ((currentElo + baseElo) / 2.0).roundToInt()
}
