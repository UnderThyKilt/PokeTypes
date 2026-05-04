package com.underthykilt.poketypes.data

enum class Difficulty(val label: String) {
    NORMAL("Normal"),
    HARD("Hard")
}

private val COMMON_TYPES = setOf(
    PokemonType.FIRE, PokemonType.WATER, PokemonType.GRASS, PokemonType.ELECTRIC,
    PokemonType.FLYING, PokemonType.GROUND, PokemonType.ROCK, PokemonType.FIGHTING,
    PokemonType.PSYCHIC, PokemonType.BUG,
)

fun filteredTypes(gen: Generation, difficulty: Difficulty): List<PokemonType> {
    val all = availableTypes(gen)
    return when (difficulty) {
        Difficulty.HARD -> all
        Difficulty.NORMAL -> all.filter { it in COMMON_TYPES }
    }
}
