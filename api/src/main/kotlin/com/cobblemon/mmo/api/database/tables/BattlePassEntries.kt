package com.cobblemon.mmo.api.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BattlePassEntries : Table("battle_pass_entries") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val seasonNumber = integer("season_number")
    val currentTier = integer("current_tier").default(0)
    val currentXp = long("current_xp").default(0L)
    val isPremium = bool("is_premium").default(false)
    val claimedTiers = text("claimed_tiers").default("[]") // JSON array of ints
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uq_bp_player_season", playerUuid, seasonNumber)
    }
}

object Banners : Table("banners") {
    val id = uuid("id")
    val name = varchar("name", 128)
    val description = text("description")
    val featuredLegendaryId = varchar("featured_legendary_id", 64)
    val featuredLegendaryName = varchar("featured_legendary_name", 64)
    val legendaryDropRate = double("legendary_drop_rate").default(0.006)
    val pityThreshold = integer("pity_threshold").default(90)
    val activeFrom = timestamp("active_from")
    val activeTo = timestamp("active_to")
    val pullCost = long("pull_cost")
    val multiPullCost = long("multi_pull_cost")
    val isActive = bool("is_active").default(false)

    override val primaryKey = PrimaryKey(id)
}

object BannerPulls : Table("banner_pulls") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val bannerId = uuid("banner_id").references(Banners.id)
    val resultSpeciesId = varchar("result_species_id", 64)
    val resultSpeciesName = varchar("result_species_name", 64)
    val resultRarity = varchar("result_rarity", 16)
    val isLegendary = bool("is_legendary").default(false)
    val wasPity = bool("was_pity").default(false)
    val pityCountAtPull = integer("pity_count_at_pull")
    val pulledAt = timestamp("pulled_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, playerUuid, bannerId)
    }
}

/** Tracks per-player pity counter per banner */
object PlayerBannerPity : Table("player_banner_pity") {
    val playerUuid = uuid("player_uuid").references(Players.id)
    val bannerId = uuid("banner_id").references(Banners.id)
    val pityCount = integer("pity_count").default(0)
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(playerUuid, bannerId)
}
