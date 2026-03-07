package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.*
import com.cobblemon.mmo.common.models.AuctionListing
import com.cobblemon.mmo.common.models.ListingStatus
import com.cobblemon.mmo.common.enums.Rarity
import com.cobblemon.mmo.common.models.Pokemon
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AuctionRepository {

    suspend fun create(listing: AuctionListing): AuctionListing = newSuspendedTransaction {
        AuctionListings.insert { row ->
            row[id] = UUID.fromString(listing.id)
            row[sellerUuid] = UUID.fromString(listing.sellerUuid)
            row[sellerName] = listing.sellerName
            row[itemType] = listing.itemType
            row[pokemonData] = listing.pokemon?.let { Json.encodeToString(Pokemon.serializer(), it) }
            row[itemId] = listing.itemId
            row[itemName] = listing.itemName
            row[itemQuantity] = listing.itemQuantity
            row[rarity] = listing.rarity.name
            row[price] = listing.price
            row[taxPaid] = listing.taxPaid
            row[status] = listing.status.name
            row[buyerUuid] = listing.buyerUuid?.let { UUID.fromString(it) }
            row[createdAt] = listing.createdAt
            row[expiresAt] = listing.expiresAt
            row[soldAt] = listing.soldAt
        }
        listing
    }

    suspend fun findById(id: String): AuctionListing? = newSuspendedTransaction {
        AuctionListings
            .selectAll()
            .where { AuctionListings.id eq UUID.fromString(id) }
            .singleOrNull()
            ?.toAuctionListing()
    }

    suspend fun findActive(
        itemType: String? = null,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        rarity: String? = null,
        speciesName: String? = null,
        limit: Int = 50,
        offset: Long = 0,
    ): List<AuctionListing> = newSuspendedTransaction {
        var query = AuctionListings.selectAll()
            .where { AuctionListings.status eq "ACTIVE" }

        itemType?.let { query = query.andWhere { AuctionListings.itemType eq it } }
        minPrice?.let { query = query.andWhere { AuctionListings.price greaterEq it } }
        maxPrice?.let { query = query.andWhere { AuctionListings.price lessEq it } }
        rarity?.let { query = query.andWhere { AuctionListings.rarity eq it } }

        query
            .orderBy(AuctionListings.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toAuctionListing() }
            .let { listings ->
                if (speciesName != null) {
                    listings.filter { it.pokemon?.speciesName?.contains(speciesName, ignoreCase = true) == true }
                } else {
                    listings
                }
            }
    }

    suspend fun findBySellerUuid(sellerUuid: String): List<AuctionListing> = newSuspendedTransaction {
        AuctionListings
            .selectAll()
            .where { AuctionListings.sellerUuid eq UUID.fromString(sellerUuid) }
            .orderBy(AuctionListings.createdAt, SortOrder.DESC)
            .map { it.toAuctionListing() }
    }

    suspend fun findHistoryByPlayerUuid(playerUuid: String): List<AuctionListing> = newSuspendedTransaction {
        val uuid = UUID.fromString(playerUuid)
        AuctionListings
            .selectAll()
            .where {
                (AuctionListings.sellerUuid eq uuid) or (AuctionListings.buyerUuid eq uuid)
            }
            .andWhere { AuctionListings.status neq "ACTIVE" }
            .orderBy(AuctionListings.soldAt, SortOrder.DESC)
            .map { it.toAuctionListing() }
    }

    suspend fun markAsSold(id: String, buyerUuid: String, soldAt: Instant): Boolean = newSuspendedTransaction {
        AuctionListings.update({ AuctionListings.id eq UUID.fromString(id) }) { row ->
            row[status] = "SOLD"
            row[AuctionListings.buyerUuid] = UUID.fromString(buyerUuid)
            row[AuctionListings.soldAt] = soldAt
        } > 0
    }

    suspend fun markAsCancelled(id: String): Boolean = newSuspendedTransaction {
        AuctionListings.update({ AuctionListings.id eq UUID.fromString(id) }) { row ->
            row[status] = "CANCELLED"
        } > 0
    }

    suspend fun expireOldListings(now: Instant): Int = newSuspendedTransaction {
        AuctionListings.update({
            (AuctionListings.status eq "ACTIVE") and (AuctionListings.expiresAt less now)
        }) { row ->
            row[status] = "EXPIRED"
        }
    }

    suspend fun countActive(): Long = newSuspendedTransaction {
        AuctionListings.selectAll()
            .where { AuctionListings.status eq "ACTIVE" }
            .count()
    }

    private fun ResultRow.toAuctionListing(): AuctionListing = AuctionListing(
        id = this[AuctionListings.id].toString(),
        sellerUuid = this[AuctionListings.sellerUuid].toString(),
        sellerName = this[AuctionListings.sellerName],
        itemType = this[AuctionListings.itemType],
        pokemon = this[AuctionListings.pokemonData]?.let { Json.decodeFromString(Pokemon.serializer(), it) },
        itemId = this[AuctionListings.itemId],
        itemName = this[AuctionListings.itemName],
        itemQuantity = this[AuctionListings.itemQuantity],
        rarity = Rarity.valueOf(this[AuctionListings.rarity]),
        price = this[AuctionListings.price],
        taxPaid = this[AuctionListings.taxPaid],
        status = ListingStatus.valueOf(this[AuctionListings.status]),
        buyerUuid = this[AuctionListings.buyerUuid]?.toString(),
        createdAt = this[AuctionListings.createdAt],
        expiresAt = this[AuctionListings.expiresAt],
        soldAt = this[AuctionListings.soldAt],
    )
}
