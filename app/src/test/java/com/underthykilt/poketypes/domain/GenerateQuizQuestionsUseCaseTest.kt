package com.underthykilt.poketypes.domain

import com.underthykilt.poketypes.data.Difficulty
import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.PresentationMode
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.SpriteGeneration
import com.underthykilt.poketypes.data.pokemon.PokemonEntry
import com.underthykilt.poketypes.data.pokemon.PokemonRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateQuizQuestionsUseCaseTest {

    // CLASSIC mode means EnrichWithPokemonUseCase is a no-op — no real PokemonRepository needed
    private val noopRepo = object : PokemonRepository {
        override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int): PokemonEntry? = null
        override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int): Boolean = true
    }

    private val useCase = GenerateQuizQuestionsUseCase(EnrichWithPokemonUseCase(noopRepo))
    private val classic = PresentationMode.CLASSIC
    private val allSprites = SpriteGeneration.ALL

    // --- count correctness ---

    @Test
    fun builds_ten_single_questions() {
        val questions = useCase.buildInitialQuestions(10, Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        assertEquals(10, questions.size)
    }

    @Test
    fun builds_ten_double_questions() {
        val questions = useCase.buildInitialQuestions(10, Generation.GEN6_PLUS, QuizMode.DOUBLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        assertEquals(10, questions.size)
    }

    @Test
    fun builds_ten_reverse_single_questions() {
        val questions = useCase.buildInitialQuestions(10, Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        assertEquals(10, questions.size)
    }

    @Test
    fun builds_ten_reverse_double_questions() {
        val questions = useCase.buildInitialQuestions(10, Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        assertEquals(10, questions.size)
    }

    // --- seen-key tracking ---

    @Test
    fun seen_keys_are_populated_after_build() {
        val seenKeys = mutableSetOf<Any>()
        useCase.buildInitialQuestions(10, Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, seenKeys)
        assertTrue(seenKeys.isNotEmpty())
    }

    @Test
    fun seen_keys_prevent_duplicate_questions_on_second_call() {
        val seenKeys = mutableSetOf<Any>()
        val first = useCase.buildInitialQuestions(5, Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, seenKeys)
        val second = useCase.buildInitialQuestions(5, Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, seenKeys)
        val firstKeys = first.map { Pair(it.attackingType, it.defendingType) }.toSet()
        val secondKeys = second.map { Pair(it.attackingType, it.defendingType) }.toSet()
        // Two separate 5-question batches sharing seenKeys should have no overlap
        assertTrue(firstKeys.intersect(secondKeys).isEmpty())
    }

    // --- generateNext ---

    @Test
    fun generate_next_returns_non_null_question() {
        val q = useCase.generateNext(Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        assertNotNull(q)
    }

    @Test
    fun generate_next_adds_to_seen_keys() {
        val seenKeys = mutableSetOf<Any>()
        useCase.generateNext(Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, seenKeys)
        assertTrue(seenKeys.isNotEmpty())
    }

    // --- generation filtering ---

    @Test
    fun gen1_questions_never_include_fairy_type() {
        val questions = useCase.buildInitialQuestions(20, Generation.GEN1, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        questions.forEach { q ->
            assertNotEquals(PokemonType.FAIRY, q.attackingType)
            assertNotEquals(PokemonType.FAIRY, q.defendingType)
        }
    }

    @Test
    fun gen1_questions_never_include_steel_type() {
        val questions = useCase.buildInitialQuestions(20, Generation.GEN1, QuizMode.SINGLE, Difficulty.HARD, classic, allSprites, mutableSetOf())
        questions.forEach { q ->
            assertNotEquals(PokemonType.STEEL, q.attackingType)
            assertNotEquals(PokemonType.STEEL, q.defendingType)
        }
    }

    @Test
    fun normal_difficulty_uses_only_common_types() {
        val commonTypes = setOf(
            PokemonType.FIRE, PokemonType.WATER, PokemonType.GRASS, PokemonType.ELECTRIC,
            PokemonType.FLYING, PokemonType.GROUND, PokemonType.ROCK, PokemonType.FIGHTING,
            PokemonType.PSYCHIC, PokemonType.BUG,
        )
        val questions = useCase.buildInitialQuestions(20, Generation.GEN6_PLUS, QuizMode.SINGLE, Difficulty.NORMAL, classic, allSprites, mutableSetOf())
        questions.forEach { q ->
            assertTrue("${q.attackingType} not in common types", q.attackingType in commonTypes)
            assertTrue("${q.defendingType} not in common types", q.defendingType in commonTypes)
        }
    }
}
