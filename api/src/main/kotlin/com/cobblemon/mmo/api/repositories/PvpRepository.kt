package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.PvpMatches
import com.cobblemon.mmo.api.database.tables.PvpRankings
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class PvpRankingRow(
    val id: String,
    val playerUuid: String,
    val seasonNumber: Int,
    val elo: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)

data class PvpMatchRow(
    val id: String,
    val seasonNumber: Int,
    val player1Uuid: String,
    val player2Uuid: String,
    val winnerUuid: String?,
    val player1EloBefore: Int,
    val player2EloBefore: Int,
    val player1EloAfter: Int,
    val player2EloAfter: Int,
    val serverName: String,
    val playedAt: Instant,
    val durationSeconds: Int,
)

class PvpRepository {

    suspend fun getOrCreateRanking(playerUuid: String, seasonNumber: Int, defaultElo: Int): PvpRankingRow =
        newSuspendedTransaction {
            val uuid = UUID.fromString(playerUuid)
            PvpRankings.selectAll()
                .where { (PvpRankings.playerUuid eq uuid) and (PvpRankings.seasonNumber eq seasonNumber) }
                .singleOrNull()
                ?.toRankingRow()
                ?: run {
                    val newId = UUID.randomUUID()
                    PvpRankings.insert { row ->
                        row[id] = newId
                        row[PvpRankings.playerUuid] = uuid
                        row[PvpRankings.seasonNumber] = seasonNumber
                        row[elo] = defaultElo
                        row[wins] = 0
                        row[losses] = 0
                        row[draws] = 0
                        row[updatedAt] = Clock.System.now()
                    }
                    PvpRankingRow(newId.toString(), playerUuid, seasonNumber, defaultElo, 0, 0, 0)
                }
        }

    suspend fun updateRanking(playerUuid: String, seasonNumber: Int, newElo: Int, isWin: Boolean, isDraw: Boolean) =
        newSuspendedTransaction {
            val uuid = UUID.fromString(playerUuid)
            PvpRankings.update({
                (PvpRankings.playerUuid eq uuid) and (PvpRankings.seasonNumber eq seasonNumber)
            }) { row ->
                row[elo] = newElo
                if (isWin) row[wins] = PvpRankings.wins + 1
                else if (isDraw) row[draws] = PvpRankings.draws + 1
                else row[losses] = PvpRankings.losses + 1
                row[updatedAt] = Clock.System.now()
            }
        }

    suspend fun getLeaderboard(seasonNumber: Int, limit: Int): List<PvpRankingRow> = newSuspendedTransaction {
        PvpRankings.selectAll()
            .where { PvpRankings.seasonNumber eq seasonNumber }
            .orderBy(PvpRankings.elo, SortOrder.DESC)
            .limit(limit)
            .map { it.toRankingRow() }
    }

    suspend fun getPlayerStats(playerUuid: String, seasonNumber: Int): PvpRankingRow? = newSuspendedTransaction {
        PvpRankings.selectAll()
            .where {
                (PvpRankings.playerUuid eq UUID.fromString(playerUuid)) and
                        (PvpRankings.seasonNumber eq seasonNumber)
            }
            .singleOrNull()
            ?.toRankingRow()
    }

    suspend fun recordMatch(match: PvpMatchRow): Unit = newSuspendedTransaction {
        PvpMatches.insert { row ->
            row[id] = UUID.fromString(match.id)
            row[seasonNumber] = match.seasonNumber
            row[player1Uuid] = UUID.fromString(match.player1Uuid)
            row[player2Uuid] = UUID.fromString(match.player2Uuid)
            row[winnerUuid] = match.winnerUuid?.let { UUID.fromString(it) }
            row[player1EloBefore] = match.player1EloBefore
            row[player2EloBefore] = match.player2EloBefore
            row[player1EloAfter] = match.player1EloAfter
            row[player2EloAfter] = match.player2EloAfter
            row[serverName] = match.serverName
            row[playedAt] = match.playedAt
            row[durationSeconds] = match.durationSeconds
        }
    }

    suspend fun softResetAllRankings(seasonNumber: Int, baseElo: Int): Unit = newSuspendedTransaction {
        PvpRankings.selectAll()
            .where { PvpRankings.seasonNumber eq seasonNumber }
            .forEach { row ->
                val currentElo = row[PvpRankings.elo]
                val newElo = ((currentElo + baseElo) / 2.0).toInt()
                PvpRankings.update({ PvpRankings.id eq row[PvpRankings.id] }) { r ->
                    r[elo] = newElo
                    r[wins] = 0
                    r[losses] = 0
                    r[draws] = 0
                    r[updatedAt] = Clock.System.now()
                }
            }
    }

    private fun ResultRow.toRankingRow() = PvpRankingRow(
        id = this[PvpRankings.id].toString(),
        playerUuid = this[PvpRankings.playerUuid].toString(),
        seasonNumber = this[PvpRankings.seasonNumber],
        elo = this[PvpRankings.elo],
        wins = this[PvpRankings.wins],
        losses = this[PvpRankings.losses],
        draws = this[PvpRankings.draws],
    )
}
