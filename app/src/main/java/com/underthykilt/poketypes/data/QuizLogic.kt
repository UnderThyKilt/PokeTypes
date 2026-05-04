package com.underthykilt.poketypes.data

enum class QuizMode(val label: String, val description: String) {
    SINGLE("Single Type", "One defending type"),
    DOUBLE("Dual Type", "Two defending types")
}

data class QuizQuestion(
    val attackingType: PokemonType,
    val defendingType: PokemonType,
    val defendingType2: PokemonType? = null,
    val correctAnswer: Float,
)

fun generateQuestion(gen: Generation, mode: QuizMode): QuizQuestion {
    val types = availableTypes(gen)
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

fun performanceMessage(score: Int): String = when {
    score == QUIZ_LENGTH -> "Perfect!"
    score >= 9 -> "Outstanding!"
    score >= 7 -> "Great job!"
    score >= 5 -> "Keep it up!"
    score >= 3 -> "Keep practicing!"
    else -> "Study the chart!"
}
