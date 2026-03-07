package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.middleware.AuctionExpiredException
import com.cobblemon.mmo.api.middleware.AuctionNotFoundException
import com.cobblemon.mmo.api.middleware.InsufficientBalanceException
import com.cobblemon.mmo.api.middleware.ValidationException
import com.cobblemon.mmo.api.repositories.AuctionRepository
import com.cobblemon.mmo.api.repositories.PlayerRepository
import com.cobblemon.mmo.api.redis.cache.AuctionCache
import com.cobblemon.mmo.common.models.AuctionListing
import com.cobblemon.mmo.common.models.ListingStatus
import com.cobblemon.mmo.common.models.Pokemon
import com.cobblemon.mmo.common.enums.Rarity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Duration.Companion.hours

private const val LISTING_TAX_PERCENT = 5
private const val LISTING_DURATION_HOURS = 48L
private const val MAX_PRICE = 1_000_000_000L
private const val MIN_PRICE = 1L

class AuctionService(
    private val auctionRepository: AuctionRepository,
    private val playerRepository: PlayerRepository,
    private val auctionCache: AuctionCache,
) {
    private val logger = LoggerFactory.getLogger(AuctionService::class.java)

    /**
     * Creates a new auction listing. Deducts the tax from the seller's balance.
     */
    suspend fun createListing(
        sellerUuid: String,
        itemType: String,
        pokemon: Pokemon?,
        itemId: String?,
        itemName: String?,
        itemQuantity: Int,
        price: Long,
    ): AuctionListing {
        if (price < MIN_PRICE || price > MAX_PRICE)
            throw ValidationException("Price must be between $MIN_PRICE and $MAX_PRICE")
        if (itemType != "POKEMON" && itemType != "ITEM")
            throw ValidationException("itemType must be 'POKEMON' or 'ITEM'")
        if (itemType == "POKEMON" && pokemon == null)
            throw ValidationException("Pokemon data is required for POKEMON listing")
        if (itemType == "ITEM" && (itemId.isNullOrBlank() || itemName.isNullOrBlank()))
            throw ValidationException("itemId and itemName are required for ITEM listing")

        val seller = playerRepository.findByUuid(sellerUuid)
            ?: throw com.cobblemon.mmo.api.middleware.PlayerNotFoundException(sellerUuid)

        val tax = (price * LISTING_TAX_PERCENT / 100L).coerceAtLeast(1L)
        if (seller.balance < tax)
            throw InsufficientBalanceException(required = tax, actual = seller.balance)

        val now = Clock.System.now()
        val listing = AuctionListing(
            id = UUID.randomUUID().toString(),
            sellerUuid = sellerUuid,
            sellerName = seller.username,
            itemType = itemType,
            pokemon = pokemon,
            itemId = itemId,
            itemName = itemName,
            itemQuantity = itemQuantity,
            rarity = pokemon?.rarity ?: Rarity.COMMON,
            price = price,
            taxPaid = tax,
            status = ListingStatus.ACTIVE,
            createdAt = now,
            expiresAt = now + LISTING_DURATION_HOURS.hours,
        )

        // Deduct tax from seller
        playerRepository.adjustBalance(sellerUuid, -tax)
        auctionRepository.create(listing)
        auctionCache.invalidate()

        logger.info("Auction listing created: ${listing.id} by $sellerUuid for $price (tax $tax)")
        return listing
    }

    /**
     * Returns active listings from cache or database.
     */
    suspend fun getActiveListings(
        itemType: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        rarity: String? = null,
        speciesName: String? = null,
    ): List<AuctionListing> {
        // Cache only works for unfiltered requests
        if (itemType == null && minPrice == null && maxPrice == null && rarity == null && speciesName == null) {
            auctionCache.getCachedListings()?.let { return it }
        }

        val listings = auctionRepository.findActive(itemType, minPrice, maxPrice, rarity, speciesName)

        if (itemType == null && minPrice == null && maxPrice == null && rarity == null && speciesName == null) {
            auctionCache.cacheListings(listings)
        }

        return listings
    }

    /**
     * Atomic buy: debit buyer, credit seller, update listing.
     */
    suspend fun buyListing(listingId: String, buyerUuid: String): AuctionListing {
        val listing = auctionRepository.findById(listingId)
            ?: throw AuctionNotFoundException(listingId)

        if (listing.status == ListingStatus.EXPIRED)
            throw AuctionExpiredException(listingId)
        if (listing.status != ListingStatus.ACTIVE)
            throw AuctionNotFoundException(listingId)

        val now = Clock.System.now()
        if (now > listing.expiresAt)
            throw AuctionExpiredException(listingId)

        if (listing.sellerUuid == buyerUuid)
            throw ValidationException("You cannot buy your own listing")

        val buyer = playerRepository.findByUuid(buyerUuid)
            ?: throw com.cobblemon.mmo.api.middleware.PlayerNotFoundException(buyerUuid)

        if (buyer.balance < listing.price)
            throw InsufficientBalanceException(required = listing.price, actual = buyer.balance)

        // Atomic: debit buyer, credit seller
        playerRepository.adjustBalance(buyerUuid, -listing.price)
        playerRepository.adjustBalance(listing.sellerUuid, listing.price)
        auctionRepository.markAsSold(listingId, buyerUuid, now)
        auctionCache.invalidate()

        logger.info("Listing $listingId bought by $buyerUuid for ${listing.price}")
        return listing.copy(status = ListingStatus.SOLD, buyerUuid = buyerUuid, soldAt = now)
    }

    /**
     * Cancel a listing. Seller gets the tax back.
     */
    suspend fun cancelListing(listingId: String, sellerUuid: String): AuctionListing {
        val listing = auctionRepository.findById(listingId)
            ?: throw AuctionNotFoundException(listingId)

        if (listing.sellerUuid != sellerUuid)
            throw ValidationException("You can only cancel your own listings")
        if (listing.status != ListingStatus.ACTIVE)
            throw ValidationException("Only active listings can be cancelled")

        auctionRepository.markAsCancelled(listingId)
        // Refund the tax to the seller
        playerRepository.adjustBalance(sellerUuid, listing.taxPaid)
        auctionCache.invalidate()

        logger.info("Listing $listingId cancelled by $sellerUuid, tax refunded: ${listing.taxPaid}")
        return listing.copy(status = ListingStatus.CANCELLED)
    }

    suspend fun getHistory(playerUuid: String): List<AuctionListing> =
        auctionRepository.findHistoryByPlayerUuid(playerUuid)

    /** Called by AuctionExpiryJob */
    suspend fun expireOldListings(): Int {
        val count = auctionRepository.expireOldListings(Clock.System.now())
        if (count > 0) {
            auctionCache.invalidate()
            logger.info("Expired $count auction listings")
        }
        return count
    }
}
