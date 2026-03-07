package com.cobblemon.mmo.api.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Players : Table("players") {
    val id = uuid("id")
    val username = varchar("username", 64)
    val balance = long("balance").default(0L)
    val playtimeSeconds = long("playtime_seconds").default(0L)
    val firstLogin = timestamp("first_login")
    val lastLogin = timestamp("last_login")
    val currentServer = varchar("current_server", 64).nullable()
    val isMuted = bool("is_muted").default(false)
    val muteExpiry = timestamp("mute_expiry").nullable()
    val isBanned = bool("is_banned").default(false)

    override val primaryKey = PrimaryKey(id)
}

object PvpRankings : Table("pvp_rankings") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val seasonNumber = integer("season_number")
    val elo = integer("elo").default(1000)
    val wins = integer("wins").default(0)
    val losses = integer("losses").default(0)
    val draws = integer("draws").default(0)
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
    /** Composite unique: one row per player per season */
    init {
        uniqueIndex("uq_pvp_player_season", playerUuid, seasonNumber)
    }
}

object PvpMatches : Table("pvp_matches") {
    val id = uuid("id")
    val seasonNumber = integer("season_number")
    val player1Uuid = uuid("player1_uuid").references(Players.id)
    val player2Uuid = uuid("player2_uuid").references(Players.id)
    val winnerUuid = uuid("winner_uuid").nullable()
    val player1EloBefore = integer("player1_elo_before")
    val player2EloBefore = integer("player2_elo_before")
    val player1EloAfter = integer("player1_elo_after")
    val player2EloAfter = integer("player2_elo_after")
    val serverName = varchar("server_name", 64)
    val playedAt = timestamp("played_at")
    val durationSeconds = integer("duration_seconds")

    override val primaryKey = PrimaryKey(id)
}
