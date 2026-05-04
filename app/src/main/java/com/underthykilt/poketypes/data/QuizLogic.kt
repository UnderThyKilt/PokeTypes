package com.underthykilt.poketypes.data

import com.underthykilt.poketypes.data.pokemon.PokemonEntry

enum class QuizMode(val label: String, val description: String) {
    SINGLE("Single Type", "One defending type"),
    DOUBLE("Dual Type", "Two defending types")
}

data class QuizQuestion(
    val attackingType: PokemonType,
    val defendingType: PokemonType,
    val defendingType2: PokemonType? = null,
    val correctAnswer: Float,
    val attackingPokemon: PokemonEntry? = null,
    val defendingPokemon: PokemonEntry? = null,
    val defendingPokemon2: PokemonEntry? = null,
)

fun generateQuestion(gen: Generation, mode: QuizMode, difficulty: Difficulty = Difficulty.HARD): QuizQuestion {
    val types = filteredTypes(gen, difficulty)
    val atk = types.random()
    val def = types.random()
    return when (mode) {
        QuizMode.SINGLE -> QuizQuestion(atk, def, null, getEffectiveness(atk, def, gen))
        QuizMode.DOUBLE -> {
            var def2 = types.random()
            while (def2 == def) def2 = types.random()
            val combined = getEffectiveness(atk, def, gen) * getEffectiveness(atk, def2, gen)
            QuizQuestion(atk, def, def2, combined)
        }
    }
}

fun performanceMessage(score: Int, total: Int): String {
    if (total == 0) return "Study the chart!"
    val pct = score * 100 / total
    return when {
        score == total -> "Perfect!"
        pct >= 90     -> "Outstanding!"
        pct >= 70     -> "Great job!"
        pct >= 50     -> "Keep it up!"
        pct >= 30     -> "Keep practicing!"
        else          -> "Study the chart!"
    }
}
