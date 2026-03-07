package com.cobblemon.mmo.common.models

import com.cobblemon.mmo.common.enums.Rarity
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BannerPull(
    val id: String,
    val playerUuid: String,
    val bannerId: String,
    val bannerName: String,
    val resultSpeciesId: String,
    val resultSpeciesName: String,
    val resultRarity: Rarity,
    val isLegendary: Boolean,
    val wasPity: Boolean,
    val pityCountAtPull: Int,
    val pulledAt: Instant,
)
