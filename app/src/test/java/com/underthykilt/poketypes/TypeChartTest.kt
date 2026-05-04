package com.underthykilt.poketypes

import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.getEffectiveness
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeChartTest {

    @Test
    fun gen6_fire_vs_grass_is_super_effective() {
        assertEquals(2f, getEffectiveness(PokemonType.FIRE, PokemonType.GRASS, Generation.GEN6_PLUS))
    }

    @Test
    fun gen6_water_vs_fire_is_super_effective() {
        assertEquals(2f, getEffectiveness(PokemonType.WATER, PokemonType.FIRE, Generation.GEN6_PLUS))
    }

    @Test
    fun gen1_ghost_vs_psychic_is_immune() {
        assertEquals(0f, getEffectiveness(PokemonType.GHOST, PokemonType.PSYCHIC, Generation.GEN1))
    }

    @Test
    fun gen6_ghost_vs_psychic_is_super_effective() {
        assertEquals(2f, getEffectiveness(PokemonType.GHOST, PokemonType.PSYCHIC, Generation.GEN6_PLUS))
    }

    @Test
    fun gen2to5_steel_resists_ghost() {
        assertEquals(0.5f, getEffectiveness(PokemonType.STEEL, PokemonType.GHOST, Generation.GEN2_5))
    }

    @Test
    fun gen6_steel_does_not_resist_ghost() {
        assertEquals(1f, getEffectiveness(PokemonType.STEEL, PokemonType.GHOST, Generation.GEN6_PLUS))
    }

    @Test
    fun gen2to5_steel_resists_dark() {
        assertEquals(0.5f, getEffectiveness(PokemonType.STEEL, PokemonType.DARK, Generation.GEN2_5))
    }

    @Test
    fun gen6_steel_does_not_resist_dark() {
        assertEquals(1f, getEffectiveness(PokemonType.STEEL, PokemonType.DARK, Generation.GEN6_PLUS))
    }

    @Test
    fun gen1_poison_vs_bug_is_super_effective() {
        assertEquals(2f, getEffectiveness(PokemonType.POISON, PokemonType.BUG, Generation.GEN1))
    }

    @Test
    fun gen6_bug_vs_poison_is_not_very_effective() {
        assertEquals(0.5f, getEffectiveness(PokemonType.BUG, PokemonType.POISON, Generation.GEN6_PLUS))
    }

    @Test
    fun normal_vs_ghost_is_immune() {
        assertEquals(0f, getEffectiveness(PokemonType.NORMAL, PokemonType.GHOST, Generation.GEN6_PLUS))
    }

    @Test
    fun dragon_vs_fairy_is_immune() {
        assertEquals(0f, getEffectiveness(PokemonType.DRAGON, PokemonType.FAIRY, Generation.GEN6_PLUS))
    }
}
