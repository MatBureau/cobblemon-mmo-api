package com.cobblemon.mmo.common.enums

import kotlinx.serialization.Serializable

@Serializable
enum class Season(val displayName: String) {
    SEASON_1("Season 1"),
    SEASON_2("Season 2"),
    SEASON_3("Season 3"),
    SEASON_4("Season 4"),
    SEASON_5("Season 5");

    companion object {
        fun fromNumber(number: Int): Season = entries.getOrNull(number - 1)
            ?: throw IllegalArgumentException("Season $number does not exist")
    }
}
