package com.cobblemon.mmo.api.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PlayerResin : Table("player_resin") {
    val playerUuid = uuid("player_uuid").references(Players.id)
    val current = integer("current").default(160)
    val max = integer("max").default(160)
    val lastRegenAt = timestamp("last_regen_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(playerUuid)
}

object DungeonSessions : Table("dungeon_sessions") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val dungeonId = varchar("dungeon_id", 64)
    val serverName = varchar("server_name", 64)
    val resinSpent = integer("resin_spent")
    val enteredAt = timestamp("entered_at")
    val completedAt = timestamp("completed_at").nullable()
    val lootData = text("loot_data").nullable() // JSON array
    val isCompleted = bool("is_completed").default(false)

    override val primaryKey = PrimaryKey(id)
}

object PokemonData : Table("pokemon_data") {
    val id = uuid("id")
    val ownerUuid = uuid("owner_uuid").references(Players.id).nullable()
    val speciesId = varchar("species_id", 64)
    val speciesName = varchar("species_name", 64)
    val nickname = varchar("nickname", 32).nullable()
    val level = integer("level").default(1)
    val nature = varchar("nature", 32)
    val primaryType = varchar("primary_type", 16)
    val secondaryType = varchar("secondary_type", 16).nullable()
    val ivsData = text("ivs_data") // JSON
    val evsData = text("evs_data") // JSON
    val movesData = text("moves_data") // JSON
    val abilityId = varchar("ability_id", 64)
    val abilityName = varchar("ability_name", 64)
    val isHiddenAbility = bool("is_hidden_ability").default(false)
    val isShiny = bool("is_shiny").default(false)
    val skinId = varchar("skin_id", 64).nullable()
    val rarity = varchar("rarity", 16).default("COMMON")
    val eggGroups = text("egg_groups").default("[]") // JSON
    val gender = varchar("gender", 12).nullable()
    val experience = long("experience").default(0L)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, ownerUuid)
    }
}

object Skins : Table("skins") {
    val id = uuid("id")
    val name = varchar("name", 128)
    val description = text("description")
    val speciesId = varchar("species_id", 64)
    val speciesName = varchar("species_name", 64)
    val rarity = varchar("rarity", 16)
    val isLimitedTime = bool("is_limited_time").default(false)
    val availableUntil = timestamp("available_until").nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}

object PlayerSkins : Table("player_skins") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val skinId = uuid("skin_id").references(Skins.id)
    val obtainedAt = timestamp("obtained_at")
    val source = varchar("source", 32) // "BANNER", "BATTLE_PASS", "EVENT", "ADMIN"

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uq_player_skin", playerUuid, skinId)
    }
}

object BreedingItems : Table("breeding_items") {
    val id = uuid("id")
    val playerUuid = uuid("player_uuid").references(Players.id)
    val itemId = varchar("item_id", 64)
    val itemName = varchar("item_name", 128)
    val quantity = integer("quantity").default(1)
    val obtainedAt = timestamp("obtained_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, playerUuid)
    }
}
