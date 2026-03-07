package com.cobblemon.mmo.api.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AuctionListings : Table("auction_listings") {
    val id = uuid("id")
    val sellerUuid = uuid("seller_uuid").references(Players.id)
    val sellerName = varchar("seller_name", 64)
    val itemType = varchar("item_type", 16) // "POKEMON" | "ITEM"
    val pokemonData = text("pokemon_data").nullable() // JSONB serialized Pokemon
    val itemId = varchar("item_id", 128).nullable()
    val itemName = varchar("item_name", 128).nullable()
    val itemQuantity = integer("item_quantity").default(1)
    val rarity = varchar("rarity", 16)
    val price = long("price")
    val taxPaid = long("tax_paid").default(0L)
    val status = varchar("status", 16).default("ACTIVE") // ACTIVE, SOLD, CANCELLED, EXPIRED
    val buyerUuid = uuid("buyer_uuid").nullable()
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val soldAt = timestamp("sold_at").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, status)
        index(false, sellerUuid)
        index(false, expiresAt)
    }
}
