package com.cobblemon.mmo.common.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PokemonNature(val statBoost: String, val statReduction: String) {
    HARDY("attack", "attack"),
    LONELY("attack", "defense"),
    BRAVE("attack", "speed"),
    ADAMANT("attack", "spAtk"),
    NAUGHTY("attack", "spDef"),
    BOLD("defense", "attack"),
    DOCILE("defense", "defense"),
    RELAXED("defense", "speed"),
    IMPISH("defense", "spAtk"),
    LAX("defense", "spDef"),
    TIMID("speed", "attack"),
    HASTY("speed", "defense"),
    SERIOUS("speed", "speed"),
    JOLLY("speed", "spAtk"),
    NAIVE("speed", "spDef"),
    MODEST("spAtk", "attack"),
    MILD("spAtk", "defense"),
    QUIET("spAtk", "speed"),
    BASHFUL("spAtk", "spAtk"),
    RASH("spAtk", "spDef"),
    CALM("spDef", "attack"),
    GENTLE("spDef", "defense"),
    SASSY("spDef", "speed"),
    CAREFUL("spDef", "spAtk"),
    QUIRKY("spDef", "spDef");

    /** Returns the stat multiplier for the given stat name (1.1, 0.9, or 1.0). */
    fun multiplierFor(stat: String): Double = when {
        stat == statBoost && statBoost != statReduction -> 1.1
        stat == statReduction && statBoost != statReduction -> 0.9
        else -> 1.0
    }
}
