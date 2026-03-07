package com.cobblemon.mmo.api.services

import com.cobblemon.mmo.api.middleware.*
import com.cobblemon.mmo.api.redis.cache.AuctionCache
import com.cobblemon.mmo.api.repositories.AuctionRepository
import com.cobblemon.mmo.api.repositories.PlayerRepository
import com.cobblemon.mmo.common.enums.Rarity
import com.cobblemon.mmo.common.models.AuctionListing
import com.cobblemon.mmo.common.models.ListingStatus
import com.cobblemon.mmo.common.models.Player
import io.mockk.*
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.hours

class AuctionServiceTest {

    private val auctionRepository = mockk<AuctionRepository>()
    private val playerRepository = mockk<PlayerRepository>()
    private val auctionCache = mockk<AuctionCache>()
    private val service = AuctionService(auctionRepository, playerRepository, auctionCache)

    private fun mockPlayer(uuid: String, balance: Long) = Player(
        uuid = uuid,
        username = "TestUser",
        balance = balance,
        playtimeSeconds = 0L,
        firstLogin = Clock.System.now(),
        lastLogin = Clock.System.now(),
    )

    private fun mockListing(
        id: String = "listing-1",
        sellerUuid: String = "seller-uuid",
        price: Long = 100L,
        status: ListingStatus = ListingStatus.ACTIVE,
        taxPaid: Long = 5L,
    ) = AuctionListing(
        id = id,
        sellerUuid = sellerUuid,
        sellerName = "Seller",
        itemType = "ITEM",
        itemId = "iron_sword",
        itemName = "Iron Sword",
        rarity = Rarity.COMMON,
        price = price,
        taxPaid = taxPaid,
        status = status,
        createdAt = Clock.System.now(),
        expiresAt = Clock.System.now() + 48.hours,
    )

    @BeforeTest
    fun setup() {
        coEvery { auctionCache.invalidate() } just Runs
        coEvery { auctionCache.getCachedListings() } returns null
        coEvery { auctionCache.cacheListings(any()) } just Runs
    }

    @Test
    fun `createListing - happy path deducts tax and creates listing`() = runTest {
        val seller = mockPlayer("seller-uuid", 1000L)
        coEvery { playerRepository.findByUuid("seller-uuid") } returns seller
        coEvery { playerRepository.adjustBalance("seller-uuid", any()) } returns 995L
        coEvery { auctionRepository.create(any()) } answers { firstArg() }

        val result = service.createListing(
            sellerUuid = "seller-uuid",
            itemType = "ITEM",
            pokemon = null,
            itemId = "sword",
            itemName = "Sword",
            itemQuantity = 1,
            price = 100L,
        )

        assertEquals(ListingStatus.ACTIVE, result.status)
        assertEquals(5L, result.taxPaid) // 5% of 100
        coVerify { playerRepository.adjustBalance("seller-uuid", -5L) }
        coVerify { auctionRepository.create(any()) }
        coVerify { auctionCache.invalidate() }
    }

    @Test
    fun `createListing - throws InsufficientBalanceException when player cannot afford tax`() = runTest {
        val seller = mockPlayer("seller-uuid", 0L) // cannot afford 5L tax
        coEvery { playerRepository.findByUuid("seller-uuid") } returns seller

        assertFailsWith<InsufficientBalanceException> {
            service.createListing("seller-uuid", "ITEM", null, "sword", "Sword", 1, 100L)
        }
    }

    @Test
    fun `createListing - throws ValidationException for invalid price`() = runTest {
        assertFailsWith<ValidationException> {
            service.createListing("seller-uuid", "ITEM", null, "sword", "Sword", 1, 0L)
        }
    }

    @Test
    fun `buyListing - happy path transfers balance`() = runTest {
        val listing = mockListing(sellerUuid = "seller-uuid", price = 100L)
        val buyer = mockPlayer("buyer-uuid", 200L)
        coEvery { auctionRepository.findById("listing-1") } returns listing
        coEvery { playerRepository.findByUuid("buyer-uuid") } returns buyer
        coEvery { playerRepository.adjustBalance(any(), any()) } returns 0L
        coEvery { auctionRepository.markAsSold(any(), any(), any()) } returns true

        val result = service.buyListing("listing-1", "buyer-uuid")

        assertEquals(ListingStatus.SOLD, result.status)
        assertEquals("buyer-uuid", result.buyerUuid)
        coVerify { playerRepository.adjustBalance("buyer-uuid", -100L) }
        coVerify { playerRepository.adjustBalance("seller-uuid", 100L) }
    }

    @Test
    fun `buyListing - throws InsufficientBalanceException when buyer cannot afford`() = runTest {
        val listing = mockListing(price = 500L)
        val buyer = mockPlayer("buyer-uuid", 100L)
        coEvery { auctionRepository.findById("listing-1") } returns listing
        coEvery { playerRepository.findByUuid("buyer-uuid") } returns buyer

        assertFailsWith<InsufficientBalanceException> {
            service.buyListing("listing-1", "buyer-uuid")
        }
    }

    @Test
    fun `buyListing - throws AuctionNotFoundException when listing sold`() = runTest {
        val listing = mockListing(status = ListingStatus.SOLD)
        coEvery { auctionRepository.findById("listing-1") } returns listing

        assertFailsWith<AuctionNotFoundException> {
            service.buyListing("listing-1", "buyer-uuid")
        }
    }

    @Test
    fun `cancelListing - refunds tax to seller`() = runTest {
        val listing = mockListing(sellerUuid = "seller-uuid", taxPaid = 5L)
        coEvery { auctionRepository.findById("listing-1") } returns listing
        coEvery { auctionRepository.markAsCancelled("listing-1") } returns true
        coEvery { playerRepository.adjustBalance("seller-uuid", 5L) } returns 105L

        val result = service.cancelListing("listing-1", "seller-uuid")

        assertEquals(ListingStatus.CANCELLED, result.status)
        coVerify { playerRepository.adjustBalance("seller-uuid", 5L) }
    }

    @Test
    fun `cancelListing - throws ValidationException when non-owner tries to cancel`() = runTest {
        val listing = mockListing(sellerUuid = "seller-uuid")
        coEvery { auctionRepository.findById("listing-1") } returns listing

        assertFailsWith<ValidationException> {
            service.cancelListing("listing-1", "other-uuid")
        }
    }
}

// Helper for running coroutines in tests
fun runTest(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
