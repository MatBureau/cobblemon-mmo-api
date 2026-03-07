package com.cobblemon.mmo.api.config

import io.ktor.server.config.*

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val auth: AuthConfig,
    val game: GameConfig,
) {
    companion object {
        fun from(config: ApplicationConfig): AppConfig = AppConfig(
            database = DatabaseConfig(
                url = config.property("database.url").getString(),
                user = config.property("database.user").getString(),
                password = config.property("database.password").getString(),
                maxPoolSize = config.property("database.maxPoolSize").getString().toInt(),
            ),
            redis = RedisConfig(
                url = config.property("redis.url").getString(),
            ),
            auth = AuthConfig(
                serverSecret = config.property("auth.serverSecret").getString(),
                adminToken = config.property("auth.adminToken").getString(),
            ),
            game = GameConfig(
                resin = ResinGameConfig(
                    maxResin = config.property("game.resin.maxResin").getString().toInt(),
                    regenIntervalMinutes = config.property("game.resin.regenIntervalMinutes").getString().toInt(),
                    dungeonCost = config.property("game.resin.dungeonCost").getString().toInt(),
                ),
                pvp = PvpGameConfig(
                    defaultElo = config.property("game.pvp.defaultElo").getString().toInt(),
                    kFactor = config.property("game.pvp.kFactor").getString().toInt(),
                    seasonDurationDays = config.property("game.pvp.seasonDurationDays").getString().toInt(),
                ),
                banner = BannerGameConfig(
                    rotationDays = config.property("game.banner.rotationDays").getString().toInt(),
                    pityThreshold = config.property("game.banner.pityThreshold").getString().toInt(),
                    legendaryDropRate = config.property("game.banner.legendaryDropRate").getString().toDouble(),
                ),
                battlePass = BattlePassGameConfig(
                    maxTier = config.property("game.battlePass.maxTier").getString().toInt(),
                    xpPerTier = config.property("game.battlePass.xpPerTier").getString().toLong(),
                ),
            ),
        )
    }
}

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
)

data class RedisConfig(val url: String)

data class AuthConfig(
    val serverSecret: String,
    val adminToken: String,
)

data class GameConfig(
    val resin: ResinGameConfig,
    val pvp: PvpGameConfig,
    val banner: BannerGameConfig,
    val battlePass: BattlePassGameConfig,
)

data class ResinGameConfig(
    val maxResin: Int,
    val regenIntervalMinutes: Int,
    val dungeonCost: Int,
)

data class PvpGameConfig(
    val defaultElo: Int,
    val kFactor: Int,
    val seasonDurationDays: Int,
)

data class BannerGameConfig(
    val rotationDays: Int,
    val pityThreshold: Int,
    val legendaryDropRate: Double,
)

data class BattlePassGameConfig(
    val maxTier: Int,
    val xpPerTier: Long,
)
