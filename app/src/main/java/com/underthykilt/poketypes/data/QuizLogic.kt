package com.underthykilt.poketypes.data

import com.underthykilt.poketypes.data.pokemon.PokemonEntry

enum class QuizMode(val label: String, val description: String) {
    SINGLE("Single Type", "One defending type"),
    DOUBLE("Dual Type", "Two defending types"),
    REVERSE_SINGLE("Reverse Single", "Pick the defending type"),
    REVERSE_DOUBLE("Reverse Dual", "Pick the defending type pair"),
}

internal val SINGLE_CHOICES = listOf(0f, 0.5f, 1f, 2f)
internal val DOUBLE_CHOICES = listOf(0f, 0.25f, 0.5f, 1f, 2f, 4f)

data class QuizQuestion(
    val attackingType: PokemonType,
    val defendingType: PokemonType,
    val defendingType2: PokemonType? = null,
    val correctAnswer: Float,
    val attackingPokemon: PokemonEntry? = null,
    val defendingPokemon: PokemonEntry? = null,
    val defendingPokemon2: PokemonEntry? = null,
    val answerChoices: List<Pair<PokemonType, PokemonType?>> = emptyList(),
    val answerChoiceMultipliers: List<Float> = emptyList(),
    val promptMultiplier: Float? = null,
)

fun generateQuestion(gen: Generation, mode: QuizMode, difficulty: Difficulty = Difficulty.HARD): QuizQuestion {
    val types = filteredTypes(gen, difficulty)
    val atk = types.random()
    return when (mode) {
        QuizMode.SINGLE -> {
            val def = types.random()
            QuizQuestion(atk, def, null, getEffectiveness(atk, def, gen))
        }
        QuizMode.DOUBLE -> {
            val def = types.random()
            var def2 = types.random()
            while (def2 == def) def2 = types.random()
            val combined = getEffectiveness(atk, def, gen) * getEffectiveness(atk, def2, gen)
            QuizQuestion(atk, def, def2, combined)
        }
        QuizMode.REVERSE_SINGLE -> generateReverseSingle(gen, types, atk)
        QuizMode.REVERSE_DOUBLE -> generateReverseDouble(gen, types, atk)
    }
}

private fun generateReverseSingle(
    gen: Generation,
    types: List<PokemonType>,
    atk: PokemonType,
): QuizQuestion {
    val mult = SINGLE_CHOICES.shuffled().first { m -> types.any { getEffectiveness(atk, it, gen) == m } }
    val correctDef = types.filter { getEffectiveness(atk, it, gen) == mult }.random()
    val wrong = types.filter { getEffectiveness(atk, it, gen) != mult }.shuffled().take(3)
    val allChoices: List<Pair<PokemonType, PokemonType?>> =
        (listOf(Pair(correctDef, null)) + wrong.map { Pair(it, null) }).shuffled()
    val choiceMultipliers = allChoices.map { (t, _) -> getEffectiveness(atk, t, gen) }
    return QuizQuestion(
        attackingType = atk,
        defendingType = correctDef,
        correctAnswer = allChoices.indexOf(Pair(correctDef, null)).toFloat(),
        answerChoices = allChoices,
        answerChoiceMultipliers = choiceMultipliers,
        promptMultiplier = mult,
    )
}

private fun generateReverseDouble(
    gen: Generation,
    types: List<PokemonType>,
    atk: PokemonType,
): QuizQuestion {
    val def1 = types.random()
    var def2 = types.random()
    while (def2 == def1) def2 = types.random()
    val targetMult = getEffectiveness(atk, def1, gen) * getEffectiveness(atk, def2, gen)
    val correctPair: Pair<PokemonType, PokemonType?> = Pair(def1, def2)
    val correctNorm = normPair(def1, def2)
    val wrongNormalized = mutableSetOf<Pair<PokemonType, PokemonType>>()
    val wrongPairs = mutableListOf<Pair<PokemonType, PokemonType?>>()
    var attempts = 0
    while (wrongPairs.size < 3 && attempts < 300) {
        attempts++
        val d1 = types.random()
        val d2 = types.random()
        if (d1 == d2) continue
        val norm = normPair(d1, d2)
        if (norm == correctNorm) continue
        if (getEffectiveness(atk, d1, gen) * getEffectiveness(atk, d2, gen) == targetMult) continue
        if (!wrongNormalized.add(norm)) continue
        wrongPairs.add(Pair(d1, d2))
    }
    val allChoices = (listOf(correctPair) + wrongPairs).shuffled()
    val choiceMultipliers = allChoices.map { (t1, t2) ->
        getEffectiveness(atk, t1, gen) * (if (t2 != null) getEffectiveness(atk, t2, gen) else 1f)
    }
    return QuizQuestion(
        attackingType = atk,
        defendingType = def1,
        defendingType2 = def2,
        correctAnswer = allChoices.indexOf(correctPair).toFloat(),
        answerChoices = allChoices,
        answerChoiceMultipliers = choiceMultipliers,
        promptMultiplier = targetMult,
    )
}

private fun normPair(a: PokemonType, b: PokemonType): Pair<PokemonType, PokemonType> =
    if (a.ordinal <= b.ordinal) Pair(a, b) else Pair(b, a)

internal fun questionKey(q: QuizQuestion): Any {
    val def2 = q.defendingType2
    return if (def2 == null) {
        Pair(q.attackingType, q.defendingType)
    } else {
        val lo = minOf(q.defendingType.ordinal, def2.ordinal)
        val hi = maxOf(q.defendingType.ordinal, def2.ordinal)
        Triple(q.attackingType, lo, hi)
    }
}

fun performanceMessage(score: Int, total: Int = QUIZ_LENGTH): String {
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
