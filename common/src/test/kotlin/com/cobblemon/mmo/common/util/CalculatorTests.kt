package com.cobblemon.mmo.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EloCalculatorTest {

    @Test
    fun `calculate - winner gains ELO, loser loses ELO`() {
        val (winnerElo, loserElo) = EloCalculator.calculate(1000, 1000, kFactor = 32)
        assertTrue(winnerElo > 1000, "Winner should gain ELO, got $winnerElo")
        assertTrue(loserElo < 1000, "Loser should lose ELO, got $loserElo")
    }

    @Test
    fun `calculate - upset win gives more ELO`() {
        val (underdog, _) = EloCalculator.calculate(800, 1200, kFactor = 32)
        val (favorite, _) = EloCalculator.calculate(1200, 800, kFactor = 32)
        // Underdog wins: 800 beats 1200, should gain more than favorite beating underdog
        assertTrue(underdog - 800 > favorite - 1200,
            "Underdog should gain more than favorite: underdog gained ${underdog - 800}, favorite gained ${favorite - 1200}")
    }

    @Test
    fun `calculate - draw moves both players toward each other`() {
        val (elo1, elo2) = EloCalculator.calculate(1200, 800, kFactor = 32, isDraw = true)
        assertTrue(elo1 < 1200, "Higher rated player should lose ELO in draw, got $elo1")
        assertTrue(elo2 > 800, "Lower rated player should gain ELO in draw, got $elo2")
    }

    @Test
    fun `calculate - ELO never goes below 0`() {
        val (_, loserElo) = EloCalculator.calculate(100, 2000, kFactor = 32)
        assertTrue(loserElo >= 0, "ELO cannot go below 0, got $loserElo")
    }

    @Test
    fun `softReset - averages with base ELO`() {
        assertEquals(1000, EloCalculator.softReset(1000, 1000)) // Equal stays same
        assertEquals(1250, EloCalculator.softReset(1500, 1000)) // High ELO comes down
        assertEquals(750, EloCalculator.softReset(500, 1000))   // Low ELO comes up
    }

    @Test
    fun `expectedScore - equal ratings give 50pct`() {
        val expected = EloCalculator.expectedScore(1000, 1000)
        assertEquals(0.5, expected, 0.001)
    }

    @Test
    fun `expectedScore - higher rating gives higher expected score`() {
        val expected = EloCalculator.expectedScore(1200, 1000)
        assertTrue(expected > 0.5, "1200 vs 1000: expected $expected should be > 0.5")
    }
}

class ResinCalculatorTest {
    @Test
    fun `computeCurrent - no regen when full`() {
        val now = kotlinx.datetime.Clock.System.now()
        val result = ResinCalculator.computeCurrent(160, 160, now, now, 8)
        assertEquals(160, result)
    }

    @Test
    fun `computeCurrent - regens correctly over time`() {
        val past = kotlinx.datetime.Clock.System.now() - kotlin.time.Duration.parse("40m")
        val now = kotlinx.datetime.Clock.System.now()
        val result = ResinCalculator.computeCurrent(0, 160, past, now, 8)
        assertEquals(5, result) // 40 min / 8 min per resin = 5
    }

    @Test
    fun `computeCurrent - caps at max`() {
        val past = kotlinx.datetime.Clock.System.now() - kotlin.time.Duration.parse("1000m")
        val now = kotlinx.datetime.Clock.System.now()
        val result = ResinCalculator.computeCurrent(100, 160, past, now, 8)
        assertEquals(160, result)
    }
}
