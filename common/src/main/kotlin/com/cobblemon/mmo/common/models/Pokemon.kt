package com.cobblemon.mmo.common.models

import com.cobblemon.mmo.common.enums.PokemonNature
import com.cobblemon.mmo.common.enums.PokemonType
import com.cobblemon.mmo.common.enums.Rarity
import kotlinx.serialization.Serializable

@Serializable
data class PokemonIVs(
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val spAtk: Int = 0,
    val spDef: Int = 0,
    val speed: Int = 0,
) {
    val total: Int get() = hp + attack + defense + spAtk + spDef + speed
}

@Serializable
data class PokemonEVs(
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val spAtk: Int = 0,
    val spDef: Int = 0,
    val speed: Int = 0,
) {
    val total: Int get() = hp + attack + defense + spAtk + spDef + speed
    fun isValid(): Boolean = total <= 510 && listOf(hp, attack, defense, spAtk, spDef, speed).all { it in 0..252 }
}

@Serializable
data class PokemonMove(
    val moveId: String,
    val moveName: String,
    val pp: Int,
    val maxPp: Int,
    val type: PokemonType,
)

@Serializable
data class Pokemon(
    val id: String,
    val ownerUuid: String?,
    val speciesId: String,
    val speciesName: String,
    val nickname: String? = null,
    val level: Int,
    val nature: PokemonNature,
    val primaryType: PokemonType,
    val secondaryType: PokemonType? = null,
    val ivs: PokemonIVs,
    val evs: PokemonEVs,
    val moves: List<PokemonMove>,
    val abilityId: String,
    val abilityName: String,
    val isHiddenAbility: Boolean = false,
    val isShiny: Boolean = false,
    val skinId: String? = null,
    val rarity: Rarity = Rarity.COMMON,
    val eggGroups: List<String> = emptyList(),
    val gender: String? = null, // "MALE", "FEMALE", "GENDERLESS"
    val experience: Long = 0L,
)
