package com.cobblemon.mmo.common.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
enum class MatchResult { WIN, LOSS, DRAW }

@Serializable
data class PvpMatch(
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
