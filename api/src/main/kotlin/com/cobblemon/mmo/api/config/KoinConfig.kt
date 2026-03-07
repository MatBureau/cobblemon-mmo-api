package com.cobblemon.mmo.api.config

import com.cobblemon.mmo.api.redis.RedisPublisher
import com.cobblemon.mmo.api.redis.RedisSubscriber
import com.cobblemon.mmo.api.redis.cache.AuctionCache
import com.cobblemon.mmo.api.redis.cache.ResinCache
import com.cobblemon.mmo.api.redis.cache.SessionCache
import com.cobblemon.mmo.api.repositories.*
import com.cobblemon.mmo.api.services.*
import io.ktor.server.config.*
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import org.koin.dsl.module

object KoinConfig {
    fun appModule(config: ApplicationConfig) = module {
        // Config
        single { AppConfig.from(config) }

        // Redis client
        single<RedisClient> {
            val redisUrl = config.property("redis.url").getString()
            RedisClient.create(redisUrl)
        }
        single<StatefulRedisConnection<String, String>> {
            get<RedisClient>().connect()
        }
        single<RedisAsyncCommands<String, String>> {
            get<StatefulRedisConnection<String, String>>().async()
        }

        // Redis pub/sub
        single { RedisPublisher(get()) }
        single { RedisSubscriber(get()) }

        // Redis cache
        single { AuctionCache(get()) }
        single { SessionCache(get()) }
        single { ResinCache(get()) }

        // Repositories
        single { PlayerRepository() }
        single { AuctionRepository() }
        single { PvpRepository() }
        single { BattlePassRepository() }
        single { BannerRepository() }
        single { ResinRepository() }
        single { BreedingRepository() }
        single { SkinRepository() }

        // Services
        single { PlayerService(get(), get()) }
        single { AuctionService(get(), get(), get()) }
        single { MatchmakingService(get(), get(), get()) }
        single { EloService(get(), get()) }
        single { BattlePassService(get(), get()) }
        single { BannerService(get(), get(), get()) }
        single { ResinService(get(), get()) }
        single { DungeonService(get(), get()) }
        single { BreedingService(get(), get()) }
        single { SkinService(get(), get()) }
        single { ChatService(get(), get()) }
    }
}
