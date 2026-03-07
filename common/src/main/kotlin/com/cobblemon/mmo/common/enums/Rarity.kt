package com.cobblemon.mmo.common.enums

import kotlinx.serialization.Serializable

@Serializable
enum class Rarity(val dropRate: Double, val displayColor: String) {
    COMMON(0.50, "#A8A8A8"),
    UNCOMMON(0.30, "#4CAF50"),
    RARE(0.15, "#2196F3"),
    EPIC(0.04, "#9C27B0"),
    LEGENDARY(0.006, "#FF9800");
}
