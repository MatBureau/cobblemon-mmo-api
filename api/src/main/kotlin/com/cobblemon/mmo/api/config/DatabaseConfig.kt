package com.cobblemon.mmo.api.config

import com.cobblemon.mmo.api.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(DatabaseConfig::class.java)

    fun init(config: ApplicationConfig): Database {
        val url = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

        logger.info("Connecting to database at $url")

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = maxPoolSize
            minimumIdle = 2
            connectionTimeout = 30_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                Players,
                PvpRankings,
                PvpMatches,
                AuctionListings,
                BattlePassEntries,
                BannerPulls,
                Banners,
                PlayerResin,
                PokemonData,
                Skins,
                PlayerSkins,
                BreedingItems,
                DungeonSessions,
            )
        }

        logger.info("Database initialized successfully")
        return database
    }
}
