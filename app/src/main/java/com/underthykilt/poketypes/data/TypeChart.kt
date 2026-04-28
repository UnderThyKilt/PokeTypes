package com.underthykilt.poketypes.data

import androidx.compose.ui.graphics.Color

enum class PokemonType(val displayName: String, val color: Color) {
    NORMAL("Normal", Color(0xFFA8A878)),
    FIRE("Fire", Color(0xFFF08030)),
    WATER("Water", Color(0xFF6890F0)),
    ELECTRIC("Electric", Color(0xFFF8D030)),
    GRASS("Grass", Color(0xFF78C850)),
    ICE("Ice", Color(0xFF98D8D8)),
    FIGHTING("Fighting", Color(0xFFC03028)),
    POISON("Poison", Color(0xFFA040A0)),
    GROUND("Ground", Color(0xFFE0C068)),
    FLYING("Flying", Color(0xFFA890F0)),
    PSYCHIC("Psychic", Color(0xFFF85888)),
    BUG("Bug", Color(0xFFA8B820)),
    ROCK("Rock", Color(0xFFB8A038)),
    GHOST("Ghost", Color(0xFF705898)),
    DRAGON("Dragon", Color(0xFF7038F8)),
    DARK("Dark", Color(0xFF705848)),
    STEEL("Steel", Color(0xFFB8B8D0)),
    FAIRY("Fairy", Color(0xFFEE99AC));

    val textColor: Color get() = Color.White
}

enum class Generation(val label: String, val description: String) {
    GEN1("Gen 1", "Red / Blue / Yellow"),
    GEN2_5("Gen 2–5", "Gold → Black/White"),
    GEN6_PLUS("Gen 6+", "X / Y → present")
}

fun availableTypes(gen: Generation): List<PokemonType> = when (gen) {
    Generation.GEN1 -> PokemonType.values().filter {
        it != PokemonType.STEEL && it != PokemonType.DARK && it != PokemonType.FAIRY
    }
    Generation.GEN2_5 -> PokemonType.values().filter { it != PokemonType.FAIRY }
    Generation.GEN6_PLUS -> PokemonType.values().toList()
}

// Gen 6+ is the base chart
private val gen6Chart: Map<PokemonType, Map<PokemonType, Float>> = buildGen6Chart()

// Gen 2–5: Steel also resists Ghost and Dark
private val gen2to5Chart: Map<PokemonType, Map<PokemonType, Float>> by lazy {
    gen6Chart.mapValues { it.value.toMutableMap() }.toMutableMap().also {
        it[PokemonType.STEEL]!![PokemonType.GHOST] = 0.5f
        it[PokemonType.STEEL]!![PokemonType.DARK] = 0.5f
    }.mapValues { it.value.toMap() }
}

// Gen 1: Ghost→Psychic is 0× (glitch), Poison→Bug 2×, Bug→Poison 2×
private val gen1Chart: Map<PokemonType, Map<PokemonType, Float>> by lazy {
    gen2to5Chart.mapValues { it.value.toMutableMap() }.toMutableMap().also {
        it[PokemonType.GHOST]!![PokemonType.PSYCHIC] = 0f
        it[PokemonType.POISON]!![PokemonType.BUG] = 2f
        it[PokemonType.BUG]!![PokemonType.POISON] = 2f
    }.mapValues { it.value.toMap() }
}

fun getEffectiveness(attacking: PokemonType, defending: PokemonType, gen: Generation = Generation.GEN6_PLUS): Float {
    val chart = when (gen) {
        Generation.GEN1 -> gen1Chart
        Generation.GEN2_5 -> gen2to5Chart
        Generation.GEN6_PLUS -> gen6Chart
    }
    return chart[attacking]?.get(defending) ?: 1f
}

fun multiplierLabel(mult: Float): String = when (mult) {
    0f    -> "0×"
    0.25f -> "¼×"
    0.5f  -> "½×"
    2f    -> "2×"
    4f    -> "4×"
    else  -> "1×"
}

private fun buildGen6Chart(): Map<PokemonType, Map<PokemonType, Float>> {
    val t = PokemonType.values()
    val chart = t.associateWith { t.associateWith { 1f }.toMutableMap() }.toMutableMap()

    fun set(atk: PokemonType, def: PokemonType, mult: Float) { chart[atk]!![def] = mult }

    set(PokemonType.NORMAL, PokemonType.ROCK, 0.5f)
    set(PokemonType.NORMAL, PokemonType.GHOST, 0f)
    set(PokemonType.NORMAL, PokemonType.STEEL, 0.5f)

    set(PokemonType.FIRE, PokemonType.FIRE, 0.5f)
    set(PokemonType.FIRE, PokemonType.WATER, 0.5f)
    set(PokemonType.FIRE, PokemonType.GRASS, 2f)
    set(PokemonType.FIRE, PokemonType.ICE, 2f)
    set(PokemonType.FIRE, PokemonType.BUG, 2f)
    set(PokemonType.FIRE, PokemonType.ROCK, 0.5f)
    set(PokemonType.FIRE, PokemonType.DRAGON, 0.5f)
    set(PokemonType.FIRE, PokemonType.STEEL, 2f)

    set(PokemonType.WATER, PokemonType.FIRE, 2f)
    set(PokemonType.WATER, PokemonType.WATER, 0.5f)
    set(PokemonType.WATER, PokemonType.GRASS, 0.5f)
    set(PokemonType.WATER, PokemonType.GROUND, 2f)
    set(PokemonType.WATER, PokemonType.ROCK, 2f)
    set(PokemonType.WATER, PokemonType.DRAGON, 0.5f)

    set(PokemonType.ELECTRIC, PokemonType.WATER, 2f)
    set(PokemonType.ELECTRIC, PokemonType.ELECTRIC, 0.5f)
    set(PokemonType.ELECTRIC, PokemonType.GRASS, 0.5f)
    set(PokemonType.ELECTRIC, PokemonType.GROUND, 0f)
    set(PokemonType.ELECTRIC, PokemonType.FLYING, 2f)
    set(PokemonType.ELECTRIC, PokemonType.DRAGON, 0.5f)

    set(PokemonType.GRASS, PokemonType.FIRE, 0.5f)
    set(PokemonType.GRASS, PokemonType.WATER, 2f)
    set(PokemonType.GRASS, PokemonType.GRASS, 0.5f)
    set(PokemonType.GRASS, PokemonType.POISON, 0.5f)
    set(PokemonType.GRASS, PokemonType.GROUND, 2f)
    set(PokemonType.GRASS, PokemonType.FLYING, 0.5f)
    set(PokemonType.GRASS, PokemonType.BUG, 0.5f)
    set(PokemonType.GRASS, PokemonType.ROCK, 2f)
    set(PokemonType.GRASS, PokemonType.DRAGON, 0.5f)
    set(PokemonType.GRASS, PokemonType.STEEL, 0.5f)

    set(PokemonType.ICE, PokemonType.FIRE, 0.5f)
    set(PokemonType.ICE, PokemonType.WATER, 0.5f)
    set(PokemonType.ICE, PokemonType.GRASS, 2f)
    set(PokemonType.ICE, PokemonType.ICE, 0.5f)
    set(PokemonType.ICE, PokemonType.GROUND, 2f)
    set(PokemonType.ICE, PokemonType.FLYING, 2f)
    set(PokemonType.ICE, PokemonType.DRAGON, 2f)
    set(PokemonType.ICE, PokemonType.STEEL, 0.5f)

    set(PokemonType.FIGHTING, PokemonType.NORMAL, 2f)
    set(PokemonType.FIGHTING, PokemonType.ICE, 2f)
    set(PokemonType.FIGHTING, PokemonType.POISON, 0.5f)
    set(PokemonType.FIGHTING, PokemonType.FLYING, 0.5f)
    set(PokemonType.FIGHTING, PokemonType.PSYCHIC, 0.5f)
    set(PokemonType.FIGHTING, PokemonType.BUG, 0.5f)
    set(PokemonType.FIGHTING, PokemonType.ROCK, 2f)
    set(PokemonType.FIGHTING, PokemonType.GHOST, 0f)
    set(PokemonType.FIGHTING, PokemonType.DARK, 2f)
    set(PokemonType.FIGHTING, PokemonType.STEEL, 2f)
    set(PokemonType.FIGHTING, PokemonType.FAIRY, 0.5f)

    set(PokemonType.POISON, PokemonType.GRASS, 2f)
    set(PokemonType.POISON, PokemonType.POISON, 0.5f)
    set(PokemonType.POISON, PokemonType.GROUND, 0.5f)
    set(PokemonType.POISON, PokemonType.ROCK, 0.5f)
    set(PokemonType.POISON, PokemonType.GHOST, 0.5f)
    set(PokemonType.POISON, PokemonType.STEEL, 0f)
    set(PokemonType.POISON, PokemonType.FAIRY, 2f)

    set(PokemonType.GROUND, PokemonType.FIRE, 2f)
    set(PokemonType.GROUND, PokemonType.ELECTRIC, 2f)
    set(PokemonType.GROUND, PokemonType.GRASS, 0.5f)
    set(PokemonType.GROUND, PokemonType.POISON, 2f)
    set(PokemonType.GROUND, PokemonType.FLYING, 0f)
    set(PokemonType.GROUND, PokemonType.BUG, 0.5f)
    set(PokemonType.GROUND, PokemonType.ROCK, 2f)
    set(PokemonType.GROUND, PokemonType.STEEL, 2f)

    set(PokemonType.FLYING, PokemonType.ELECTRIC, 0.5f)
    set(PokemonType.FLYING, PokemonType.GRASS, 2f)
    set(PokemonType.FLYING, PokemonType.FIGHTING, 2f)
    set(PokemonType.FLYING, PokemonType.BUG, 2f)
    set(PokemonType.FLYING, PokemonType.ROCK, 0.5f)
    set(PokemonType.FLYING, PokemonType.STEEL, 0.5f)

    set(PokemonType.PSYCHIC, PokemonType.FIGHTING, 2f)
    set(PokemonType.PSYCHIC, PokemonType.POISON, 2f)
    set(PokemonType.PSYCHIC, PokemonType.PSYCHIC, 0.5f)
    set(PokemonType.PSYCHIC, PokemonType.DARK, 0f)
    set(PokemonType.PSYCHIC, PokemonType.STEEL, 0.5f)

    set(PokemonType.BUG, PokemonType.FIRE, 0.5f)
    set(PokemonType.BUG, PokemonType.GRASS, 2f)
    set(PokemonType.BUG, PokemonType.FIGHTING, 0.5f)
    set(PokemonType.BUG, PokemonType.POISON, 0.5f)
    set(PokemonType.BUG, PokemonType.FLYING, 0.5f)
    set(PokemonType.BUG, PokemonType.PSYCHIC, 2f)
    set(PokemonType.BUG, PokemonType.GHOST, 0.5f)
    set(PokemonType.BUG, PokemonType.DARK, 2f)
    set(PokemonType.BUG, PokemonType.STEEL, 0.5f)
    set(PokemonType.BUG, PokemonType.FAIRY, 0.5f)

    set(PokemonType.ROCK, PokemonType.FIRE, 2f)
    set(PokemonType.ROCK, PokemonType.ICE, 2f)
    set(PokemonType.ROCK, PokemonType.FIGHTING, 0.5f)
    set(PokemonType.ROCK, PokemonType.GROUND, 0.5f)
    set(PokemonType.ROCK, PokemonType.FLYING, 2f)
    set(PokemonType.ROCK, PokemonType.BUG, 2f)
    set(PokemonType.ROCK, PokemonType.STEEL, 0.5f)

    set(PokemonType.GHOST, PokemonType.NORMAL, 0f)
    set(PokemonType.GHOST, PokemonType.PSYCHIC, 2f)
    set(PokemonType.GHOST, PokemonType.GHOST, 2f)
    set(PokemonType.GHOST, PokemonType.DARK, 0.5f)

    set(PokemonType.DRAGON, PokemonType.DRAGON, 2f)
    set(PokemonType.DRAGON, PokemonType.STEEL, 0.5f)
    set(PokemonType.DRAGON, PokemonType.FAIRY, 0f)

    set(PokemonType.DARK, PokemonType.FIGHTING, 0.5f)
    set(PokemonType.DARK, PokemonType.PSYCHIC, 2f)
    set(PokemonType.DARK, PokemonType.GHOST, 2f)
    set(PokemonType.DARK, PokemonType.DARK, 0.5f)
    set(PokemonType.DARK, PokemonType.FAIRY, 0.5f)

    set(PokemonType.STEEL, PokemonType.FIRE, 0.5f)
    set(PokemonType.STEEL, PokemonType.WATER, 0.5f)
    set(PokemonType.STEEL, PokemonType.ELECTRIC, 0.5f)
    set(PokemonType.STEEL, PokemonType.ICE, 2f)
    set(PokemonType.STEEL, PokemonType.ROCK, 2f)
    set(PokemonType.STEEL, PokemonType.STEEL, 0.5f)
    set(PokemonType.STEEL, PokemonType.FAIRY, 2f)

    set(PokemonType.FAIRY, PokemonType.FIRE, 0.5f)
    set(PokemonType.FAIRY, PokemonType.FIGHTING, 2f)
    set(PokemonType.FAIRY, PokemonType.POISON, 0.5f)
    set(PokemonType.FAIRY, PokemonType.DRAGON, 2f)
    set(PokemonType.FAIRY, PokemonType.DARK, 2f)
    set(PokemonType.FAIRY, PokemonType.STEEL, 0.5f)

    return chart.mapValues { it.value.toMap() }
}
