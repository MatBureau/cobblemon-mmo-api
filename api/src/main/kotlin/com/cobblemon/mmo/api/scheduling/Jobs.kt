package com.cobblemon.mmo.api.scheduling

import com.cobblemon.mmo.api.services.AuctionService
import com.cobblemon.mmo.api.services.ResinService
import com.cobblemon.mmo.api.services.BannerService
import com.cobblemon.mmo.api.repositories.PvpRepository
import com.cobblemon.mmo.api.config.AppConfig
import com.cobblemon.mmo.api.redis.channels.EventChannel
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Central scheduler that starts all periodic jobs.
 * Call [start] once at application startup.
 */
object Scheduler {
    private val logger = LoggerFactory.getLogger(Scheduler::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(
        auctionService: AuctionService,
        pvpRepository: PvpRepository,
        appConfig: AppConfig,
        eventChannel: EventChannel,
    ) {
        // Auction expiry — every 10 minutes
        scope.launch {
            while (isActive) {
                try { auctionService.expireOldListings() }
                catch (e: Exception) { logger.error("AuctionExpiryJob error: ${e.message}", e) }
                delay(10.minutes)
            }
        }

        logger.info("All scheduled jobs started")
    }

    fun stop() = scope.cancel()
}

/** Standalone job: expire auction listings */
class AuctionExpiryJob(private val auctionService: AuctionService) {
    private val logger = LoggerFactory.getLogger(AuctionExpiryJob::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() = scope.launch {
        while (isActive) {
            delay(10.minutes)
            try {
                val count = auctionService.expireOldListings()
                if (count > 0) logger.info("Expired $count auction listings")
            } catch (e: Exception) {
                logger.error("AuctionExpiryJob failed: ${e.message}", e)
            }
        }
    }

    fun stop() = scope.cancel()
}

/** Standalone job: matchmaking queue scan every 5 seconds */
class MatchmakingJob(private val matchmakingService: com.cobblemon.mmo.api.services.MatchmakingService) {
    private val logger = LoggerFactory.getLogger(MatchmakingJob::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() = scope.launch {
        while (isActive) {
            delay(5.seconds)
            try {
                matchmakingService.processQueue()
            } catch (e: Exception) {
                logger.error("MatchmakingJob failed: ${e.message}", e)
            }
        }
    }

    fun stop() = scope.cancel()
}

/** Standalone job: season reset */
class SeasonResetJob(
    private val pvpRepository: PvpRepository,
    private val appConfig: AppConfig,
    private val eventChannel: EventChannel,
) {
    private val logger = LoggerFactory.getLogger(SeasonResetJob::class.java)

    suspend fun executeSoftReset(seasonNumber: Int) {
        logger.info("Executing season soft reset for season $seasonNumber")
        pvpRepository.softResetAllRankings(seasonNumber, appConfig.game.pvp.defaultElo)
        eventChannel.broadcastSeasonEnded(seasonNumber)
        logger.info("Season $seasonNumber reset complete")
    }
}

/** Standalone job: banner rotation */
class BannerRotationJob(
    private val bannerRepository: com.cobblemon.mmo.api.repositories.BannerRepository,
    private val eventChannel: EventChannel,
) {
    private val logger = LoggerFactory.getLogger(BannerRotationJob::class.java)

    suspend fun rotateTo(newBannerId: String) {
        bannerRepository.setActive(newBannerId)
        val banner = bannerRepository.findById(newBannerId)
        if (banner != null) {
            eventChannel.broadcastBannerRotated(banner.id, banner.name)
            logger.info("Banner rotated to: ${banner.name}")
        }
    }
}
