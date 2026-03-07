package com.cobblemon.mmo.common.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PokemonType {
    NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON,
    GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY;
}
