package com.cobblemon.mmo.api.repositories

import com.cobblemon.mmo.api.database.tables.*
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

data class BannerRow(
    val id: String,
    val name: String,
    val description: String,
    val featuredLegendaryId: String,
    val featuredLegendaryName: String,
    val legendaryDropRate: Double,
    val pityThreshold: Int,
    val activeFrom: kotlinx.datetime.Instant,
    val activeTo: kotlinx.datetime.Instant,
    val pullCost: Long,
    val multiPullCost: Long,
    val isActive: Boolean,
)

class BannerRepository {
    suspend fun findActive(): BannerRow? = newSuspendedTransaction {
        Banners.selectAll().where { Banners.isActive eq true }.singleOrNull()?.toBannerRow()
    }

    suspend fun findById(id: String): BannerRow? = newSuspendedTransaction {
        Banners.selectAll().where { Banners.id eq UUID.fromString(id) }.singleOrNull()?.toBannerRow()
    }

    suspend fun create(banner: BannerRow): BannerRow = newSuspendedTransaction {
        Banners.insert { row ->
            row[id] = UUID.fromString(banner.id)
            row[name] = banner.name
            row[description] = banner.description
            row[featuredLegendaryId] = banner.featuredLegendaryId
            row[featuredLegendaryName] = banner.featuredLegendaryName
            row[legendaryDropRate] = banner.legendaryDropRate
            row[pityThreshold] = banner.pityThreshold
            row[activeFrom] = banner.activeFrom
            row[activeTo] = banner.activeTo
            row[pullCost] = banner.pullCost
            row[multiPullCost] = banner.multiPullCost
            row[isActive] = banner.isActive
        }
        banner
    }

    suspend fun setActive(bannerId: String): Unit = newSuspendedTransaction {
        Banners.update({ Banners.isActive eq true }) { row -> row[isActive] = false }
        Banners.update({ Banners.id eq UUID.fromString(bannerId) }) { row -> row[isActive] = true }
    }

    suspend fun getPity(playerUuid: String, bannerId: String): Int = newSuspendedTransaction {
        PlayerBannerPity.selectAll()
            .where {
                (PlayerBannerPity.playerUuid eq UUID.fromString(playerUuid)) and
                        (PlayerBannerPity.bannerId eq UUID.fromString(bannerId))
            }
            .singleOrNull()
            ?.get(PlayerBannerPity.pityCount) ?: 0
    }

    suspend fun updatePity(playerUuid: String, bannerId: String, newPity: Int): Unit = newSuspendedTransaction {
        val updated = PlayerBannerPity.update({
            (PlayerBannerPity.playerUuid eq UUID.fromString(playerUuid)) and
                    (PlayerBannerPity.bannerId eq UUID.fromString(bannerId))
        }) { row ->
            row[pityCount] = newPity
            row[updatedAt] = Clock.System.now()
        }
        if (updated == 0) {
            PlayerBannerPity.insert { row ->
                row[playerUuid] = UUID.fromString(playerUuid)
                row[bannerId] = UUID.fromString(bannerId)
                row[pityCount] = newPity
                row[updatedAt] = Clock.System.now()
            }
        }
    }

    suspend fun recordPull(pull: com.cobblemon.mmo.common.models.BannerPull): Unit = newSuspendedTransaction {
        BannerPulls.insert { row ->
            row[id] = UUID.fromString(pull.id)
            row[playerUuid] = UUID.fromString(pull.playerUuid)
            row[bannerId] = UUID.fromString(pull.bannerId)
            row[resultSpeciesId] = pull.resultSpeciesId
            row[resultSpeciesName] = pull.resultSpeciesName
            row[resultRarity] = pull.resultRarity.name
            row[isLegendary] = pull.isLegendary
            row[wasPity] = pull.wasPity
            row[pityCountAtPull] = pull.pityCountAtPull
            row[pulledAt] = pull.pulledAt
        }
    }

    private fun ResultRow.toBannerRow() = BannerRow(
        id = this[Banners.id].toString(),
        name = this[Banners.name],
        description = this[Banners.description],
        featuredLegendaryId = this[Banners.featuredLegendaryId],
        featuredLegendaryName = this[Banners.featuredLegendaryName],
        legendaryDropRate = this[Banners.legendaryDropRate],
        pityThreshold = this[Banners.pityThreshold],
        activeFrom = this[Banners.activeFrom],
        activeTo = this[Banners.activeTo],
        pullCost = this[Banners.pullCost],
        multiPullCost = this[Banners.multiPullCost],
        isActive = this[Banners.isActive],
    )
}
