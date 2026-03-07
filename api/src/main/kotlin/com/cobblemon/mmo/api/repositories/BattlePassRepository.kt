package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.BattlePassEntries
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class BattlePassRow(
    val id: String,
    val playerUuid: String,
    val seasonNumber: Int,
    val currentTier: Int,
    val currentXp: Long,
    val isPremium: Boolean,
    val claimedTiers: List<Int>,
)

class BattlePassRepository {
    suspend fun getOrCreate(playerUuid: String, seasonNumber: Int): BattlePassRow = newSuspendedTransaction {
        val uuid = UUID.fromString(playerUuid)
        BattlePassEntries.selectAll()
            .where { (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber) }
            .singleOrNull()
            ?.toRow()
            ?: run {
                val newId = UUID.randomUUID()
                BattlePassEntries.insert { row ->
                    row[id] = newId
                    row[BattlePassEntries.playerUuid] = uuid
                    row[BattlePassEntries.seasonNumber] = seasonNumber
                    row[currentTier] = 0
                    row[currentXp] = 0L
                    row[isPremium] = false
                    row[claimedTiers] = "[]"
                    row[updatedAt] = Clock.System.now()
                }
                BattlePassRow(newId.toString(), playerUuid, seasonNumber, 0, 0L, false, emptyList())
            }
    }

    suspend fun addXp(playerUuid: String, seasonNumber: Int, xp: Long, xpPerTier: Long, maxTier: Int): BattlePassRow =
        newSuspendedTransaction {
            val uuid = UUID.fromString(playerUuid)
            val current = BattlePassEntries.selectAll()
                .where { (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber) }
                .single()
            val totalXp = current[BattlePassEntries.currentXp] + xp
            val newTier = (totalXp / xpPerTier).toInt().coerceAtMost(maxTier)
            val remainingXp = totalXp % xpPerTier

            BattlePassEntries.update({
                (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber)
            }) { row ->
                row[currentTier] = newTier
                row[currentXp] = remainingXp
                row[updatedAt] = Clock.System.now()
            }
            current.toRow().copy(currentTier = newTier, currentXp = remainingXp)
        }

    suspend fun claimTier(playerUuid: String, seasonNumber: Int, tier: Int): Unit = newSuspendedTransaction {
        val uuid = UUID.fromString(playerUuid)
        val current = BattlePassEntries.selectAll()
            .where { (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber) }
            .single()
        val claimed = Json.decodeFromString<List<Int>>(current[BattlePassEntries.claimedTiers]).toMutableList()
        claimed.add(tier)
        BattlePassEntries.update({
            (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber)
        }) { row ->
            row[claimedTiers] = Json.encodeToString(claimed.distinct().sorted())
            row[updatedAt] = Clock.System.now()
        }
    }

    suspend fun setPremium(playerUuid: String, seasonNumber: Int): Unit = newSuspendedTransaction {
        val uuid = UUID.fromString(playerUuid)
        BattlePassEntries.update({
            (BattlePassEntries.playerUuid eq uuid) and (BattlePassEntries.seasonNumber eq seasonNumber)
        }) { row ->
            row[isPremium] = true
            row[updatedAt] = Clock.System.now()
        }
    }

    private fun ResultRow.toRow() = BattlePassRow(
        id = this[BattlePassEntries.id].toString(),
        playerUuid = this[BattlePassEntries.playerUuid].toString(),
        seasonNumber = this[BattlePassEntries.seasonNumber],
        currentTier = this[BattlePassEntries.currentTier],
        currentXp = this[BattlePassEntries.currentXp],
        isPremium = this[BattlePassEntries.isPremium],
        claimedTiers = Json.decodeFromString(this[BattlePassEntries.claimedTiers]),
    )
}
