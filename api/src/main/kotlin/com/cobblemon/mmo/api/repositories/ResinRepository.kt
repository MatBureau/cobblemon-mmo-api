package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.PlayerResin
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class ResinRow(
    val playerUuid: String,
    val current: Int,
    val max: Int,
    val lastRegenAt: Instant,
)

class ResinRepository {
    suspend fun getOrCreate(playerUuid: String, max: Int): ResinRow = newSuspendedTransaction {
        val uuid = UUID.fromString(playerUuid)
        val now = Clock.System.now()
        PlayerResin.selectAll().where { PlayerResin.playerUuid eq uuid }.singleOrNull()?.toRow()
            ?: run {
                PlayerResin.insert { row ->
                    row[PlayerResin.playerUuid] = uuid
                    row[current] = max
                    row[PlayerResin.max] = max
                    row[lastRegenAt] = now
                    row[updatedAt] = now
                }
                ResinRow(playerUuid, max, max, now)
            }
    }

    suspend fun setResin(playerUuid: String, current: Int): Unit = newSuspendedTransaction {
        val now = Clock.System.now()
        PlayerResin.update({ PlayerResin.playerUuid eq UUID.fromString(playerUuid) }) { row ->
            row[PlayerResin.current] = current
            row[lastRegenAt] = now
            row[updatedAt] = now
        }
    }

    suspend fun consumeResin(playerUuid: String, amount: Int, computedCurrent: Int): Int = newSuspendedTransaction {
        val newAmount = (computedCurrent - amount).coerceAtLeast(0)
        val now = Clock.System.now()
        PlayerResin.update({ PlayerResin.playerUuid eq UUID.fromString(playerUuid) }) { row ->
            row[current] = newAmount
            row[lastRegenAt] = now
            row[updatedAt] = now
        }
        newAmount
    }

    private fun ResultRow.toRow() = ResinRow(
        playerUuid = this[PlayerResin.playerUuid].toString(),
        current = this[PlayerResin.current],
        max = this[PlayerResin.max],
        lastRegenAt = this[PlayerResin.lastRegenAt],
    )
}
