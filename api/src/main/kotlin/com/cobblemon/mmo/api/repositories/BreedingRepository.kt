package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.BreedingItems
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class BreedingItemRow(val id: String, val playerUuid: String, val itemId: String, val itemName: String, val quantity: Int)

class BreedingRepository {
    suspend fun findAllByPlayer(playerUuid: String): List<BreedingItemRow> = newSuspendedTransaction {
        BreedingItems.selectAll()
            .where { BreedingItems.playerUuid eq UUID.fromString(playerUuid) }
            .map { it.toRow() }
    }

    suspend fun findByPlayerAndItem(playerUuid: String, itemId: String): BreedingItemRow? = newSuspendedTransaction {
        BreedingItems.selectAll()
            .where { (BreedingItems.playerUuid eq UUID.fromString(playerUuid)) and (BreedingItems.itemId eq itemId) }
            .singleOrNull()?.toRow()
    }

    suspend fun decrementItem(playerUuid: String, itemId: String): Boolean = newSuspendedTransaction {
        val row = BreedingItems.selectAll()
            .where { (BreedingItems.playerUuid eq UUID.fromString(playerUuid)) and (BreedingItems.itemId eq itemId) }
            .singleOrNull() ?: return@newSuspendedTransaction false
        val qty = row[BreedingItems.quantity]
        if (qty <= 1) {
            BreedingItems.deleteWhere { BreedingItems.id eq row[BreedingItems.id] }
        } else {
            BreedingItems.update({ BreedingItems.id eq row[BreedingItems.id] }) { r -> r[quantity] = qty - 1 }
        }
        true
    }

    private fun ResultRow.toRow() = BreedingItemRow(
        id = this[BreedingItems.id].toString(),
        playerUuid = this[BreedingItems.playerUuid].toString(),
        itemId = this[BreedingItems.itemId],
        itemName = this[BreedingItems.itemName],
        quantity = this[BreedingItems.quantity],
    )
}

class SkinRepository {
    suspend fun findPlayerSkins(playerUuid: String): List<String> = newSuspendedTransaction {
        com.cobblemon.mmo.api.database.tables.PlayerSkins.selectAll()
            .where { com.cobblemon.mmo.api.database.tables.PlayerSkins.playerUuid eq UUID.fromString(playerUuid) }
            .map { it[com.cobblemon.mmo.api.database.tables.PlayerSkins.skinId].toString() }
    }

    suspend fun playerOwnsSkin(playerUuid: String, skinId: String): Boolean = newSuspendedTransaction {
        com.cobblemon.mmo.api.database.tables.PlayerSkins.selectAll()
            .where {
                (com.cobblemon.mmo.api.database.tables.PlayerSkins.playerUuid eq UUID.fromString(playerUuid)) and
                        (com.cobblemon.mmo.api.database.tables.PlayerSkins.skinId eq UUID.fromString(skinId))
            }
            .any()
    }

    suspend fun grantSkin(playerUuid: String, skinId: String, source: String): Unit = newSuspendedTransaction {
        com.cobblemon.mmo.api.database.tables.PlayerSkins.insert { row ->
            row[id] = UUID.randomUUID()
            row[com.cobblemon.mmo.api.database.tables.PlayerSkins.playerUuid] = UUID.fromString(playerUuid)
            row[com.cobblemon.mmo.api.database.tables.PlayerSkins.skinId] = UUID.fromString(skinId)
            row[obtainedAt] = Clock.System.now()
            row[com.cobblemon.mmo.api.database.tables.PlayerSkins.source] = source
        }
    }
}
