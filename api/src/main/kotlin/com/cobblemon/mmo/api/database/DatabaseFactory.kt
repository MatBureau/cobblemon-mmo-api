package com.cobblemon.mmo.api.database

import com.cobblemon.mmo.api.database.tables.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(config: ApplicationConfig): Database {
        val database = com.cobblemon.mmo.api.config.DatabaseConfig.init(config)
        logger.info("DatabaseFactory: all tables created")
        return database
    }

    /** Convenience for test setup — accepts a raw Database object */
    fun createSchema(database: Database) {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                Players,
                PvpRankings,
                PvpMatches,
                AuctionListings,
                BattlePassEntries,
                Banners,
                BannerPulls,
                PlayerBannerPity,
                PlayerResin,
                DungeonSessions,
                PokemonData,
                Skins,
                PlayerSkins,
                BreedingItems,
            )
        }
    }

    fun dropAll(database: Database) {
        transaction(database) {
            SchemaUtils.drop(
                Players,
                PvpRankings,
                PvpMatches,
                AuctionListings,
                BattlePassEntries,
                Banners,
                BannerPulls,
                PlayerBannerPity,
                PlayerResin,
                DungeonSessions,
                PokemonData,
                Skins,
                PlayerSkins,
                BreedingItems,
            )
        }
    }
}
