package com.cobblemon.mmo.common.models

import com.cobblemon.mmo.common.enums.Rarity
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class ListingStatus { ACTIVE, SOLD, CANCELLED, EXPIRED }

@Serializable
data class AuctionListing(
    val id: String,
    val sellerUuid: String,
    val sellerName: String,
    val itemType: String, // "POKEMON" | "ITEM"
    val pokemon: Pokemon? = null,
    val itemId: String? = null,
    val itemName: String? = null,
    val itemQuantity: Int = 1,
    val rarity: Rarity,
    val price: Long,
    val taxPaid: Long,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val buyerUuid: String? = null,
    val createdAt: Instant,
    val expiresAt: Instant,
    val soldAt: Instant? = null,
)
