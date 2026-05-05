package com.underthykilt.poketypes.domain

import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.data.pokemon.PokemonEntry
import com.underthykilt.poketypes.data.pokemon.PokemonRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnrichWithPokemonUseCaseTest {

    private val fakePokemon = PokemonEntry(id = 1, name = "Bulbasaur", generation = 1)

    private val alwaysFoundRepo = object : PokemonRepository {
        override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int) = fakePokemon
        override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int) = true
    }

    private val neverFoundRepo = object : PokemonRepository {
        override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int) = null
        override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int) = false
    }

    private val singleTypeQuestion = QuizQuestion(
        attackingType = PokemonType.FIRE,
        defendingType = PokemonType.GRASS,
        correctAnswer = 2f,
    )

    private val dualTypeQuestion = singleTypeQuestion.copy(defendingType2 = PokemonType.WATER)

    private val reverseQuestion = singleTypeQuestion.copy(
        answerChoices = listOf(Pair(PokemonType.GRASS, null)),
        correctAnswer = 0f,
        promptMultiplier = 2f,
    )

    // --- classic mode (no-op) ---

    @Test
    fun enrich_classic_mode_does_not_set_attacking_pokemon() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(singleTypeQuestion, PresentationMode.CLASSIC, SpriteGeneration.ALL)
        assertNull(result.attackingPokemon)
    }

    @Test
    fun enrich_classic_mode_does_not_set_defending_pokemon() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(singleTypeQuestion, PresentationMode.CLASSIC, SpriteGeneration.ALL)
        assertNull(result.defendingPokemon)
    }

    // --- pokemon mode: forward questions ---

    @Test
    fun enrich_pokemon_mode_sets_attacking_pokemon() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(singleTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL)
        assertEquals(fakePokemon, result.attackingPokemon)
    }

    @Test
    fun enrich_pokemon_mode_sets_defending_pokemon_for_single_type() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(singleTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL)
        assertEquals(fakePokemon, result.defendingPokemon)
    }

    @Test
    fun enrich_pokemon_mode_sets_defending_pokemon_for_dual_type() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(dualTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL)
        assertEquals(fakePokemon, result.defendingPokemon)
    }

    // --- pokemon mode: reverse questions ---

    @Test
    fun enrich_reverse_mode_sets_attacking_pokemon() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(reverseQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL)
        assertEquals(fakePokemon, result.attackingPokemon)
    }

    @Test
    fun enrich_reverse_mode_does_not_set_defending_pokemon() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        val result = useCase.enrich(reverseQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL)
        assertNull(result.defendingPokemon)
    }

    // --- hasRequiredPokemon ---

    @Test
    fun has_required_pokemon_always_true_in_classic_mode() {
        val useCase = EnrichWithPokemonUseCase(neverFoundRepo)
        assertTrue(useCase.hasRequiredPokemon(singleTypeQuestion, PresentationMode.CLASSIC, SpriteGeneration.ALL))
    }

    @Test
    fun has_required_pokemon_false_when_attacker_not_found() {
        val useCase = EnrichWithPokemonUseCase(neverFoundRepo)
        assertFalse(useCase.hasRequiredPokemon(singleTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL))
    }

    @Test
    fun has_required_pokemon_true_when_attacker_and_defender_found() {
        val useCase = EnrichWithPokemonUseCase(alwaysFoundRepo)
        assertTrue(useCase.hasRequiredPokemon(singleTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL))
    }

    @Test
    fun has_required_pokemon_true_for_reverse_when_only_attacker_found() {
        // reverse mode only requires the attacking Pokémon
        val attackerOnlyRepo = object : PokemonRepository {
            override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int): PokemonEntry? =
                fakePokemon.takeIf { types.toSet() == setOf(PokemonType.FIRE) }
            override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int): Boolean =
                types.toSet() == setOf(PokemonType.FIRE)
        }
        val useCase = EnrichWithPokemonUseCase(attackerOnlyRepo)
        assertTrue(useCase.hasRequiredPokemon(reverseQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL))
    }

    @Test
    fun has_required_pokemon_false_for_forward_when_defender_not_found() {
        val attackerOnlyRepo = object : PokemonRepository {
            override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int): PokemonEntry? =
                fakePokemon.takeIf { types.toSet() == setOf(PokemonType.FIRE) }
            override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int): Boolean =
                types.toSet() == setOf(PokemonType.FIRE)
        }
        val useCase = EnrichWithPokemonUseCase(attackerOnlyRepo)
        assertFalse(useCase.hasRequiredPokemon(singleTypeQuestion, PresentationMode.POKEMON, SpriteGeneration.ALL))
    }
}
