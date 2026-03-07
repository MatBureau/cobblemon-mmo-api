package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.Players
import com.cobblemon.mmo.common.models.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class PlayerRepository {

    suspend fun findByUuid(uuid: String): Player? = newSuspendedTransaction {
        Players.selectAll()
            .where { Players.id eq UUID.fromString(uuid) }
            .singleOrNull()
            ?.toPlayer()
    }

    suspend fun findByUsername(username: String): Player? = newSuspendedTransaction {
        Players.selectAll()
            .where { Players.username eq username }
            .singleOrNull()
            ?.toPlayer()
    }

    suspend fun createOrUpdate(uuid: String, username: String, serverName: String): Player = newSuspendedTransaction {
        val now = Clock.System.now()
        val existing = Players.selectAll().where { Players.id eq UUID.fromString(uuid) }.singleOrNull()

        if (existing == null) {
            Players.insert { row ->
                row[id] = UUID.fromString(uuid)
                row[Players.username] = username
                row[balance] = 0L
                row[playtimeSeconds] = 0L
                row[firstLogin] = now
                row[lastLogin] = now
                row[currentServer] = serverName
                row[isMuted] = false
                row[isBanned] = false
            }
            Player(uuid = uuid, username = username, firstLogin = now, lastLogin = now, currentServer = serverName)
        } else {
            Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
                row[Players.username] = username
                row[lastLogin] = now
                row[currentServer] = serverName
            }
            existing.toPlayer().copy(username = username, lastLogin = now, currentServer = serverName)
        }
    }

    suspend fun adjustBalance(uuid: String, delta: Long): Long = newSuspendedTransaction {
        val current = Players.selectAll()
            .where { Players.id eq UUID.fromString(uuid) }
            .single()[Players.balance]
        val newBalance = (current + delta).coerceAtLeast(0L)
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[balance] = newBalance
        }
        newBalance
    }

    suspend fun setBalance(uuid: String, amount: Long): Unit = newSuspendedTransaction {
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[balance] = amount.coerceAtLeast(0L)
        }
    }

    suspend fun setServer(uuid: String, serverName: String?): Unit = newSuspendedTransaction {
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[currentServer] = serverName
        }
    }

    suspend fun incrementPlaytime(uuid: String, seconds: Long): Unit = newSuspendedTransaction {
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[playtimeSeconds] = Players.playtimeSeconds + seconds
        }
    }

    suspend fun mute(uuid: String, expiry: Instant): Unit = newSuspendedTransaction {
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[isMuted] = true
            row[muteExpiry] = expiry
        }
    }

    suspend fun unmute(uuid: String): Unit = newSuspendedTransaction {
        Players.update({ Players.id eq UUID.fromString(uuid) }) { row ->
            row[isMuted] = false
            row[muteExpiry] = null
        }
    }

    suspend fun countTotal(): Long = newSuspendedTransaction {
        Players.selectAll().count()
    }

    private fun ResultRow.toPlayer(): Player = Player(
        uuid = this[Players.id].toString(),
        username = this[Players.username],
        balance = this[Players.balance],
        playtimeSeconds = this[Players.playtimeSeconds],
        firstLogin = this[Players.firstLogin],
        lastLogin = this[Players.lastLogin],
        currentServer = this[Players.currentServer],
        isMuted = this[Players.isMuted],
        muteExpiry = this[Players.muteExpiry],
        isBanned = this[Players.isBanned],
    )
}
