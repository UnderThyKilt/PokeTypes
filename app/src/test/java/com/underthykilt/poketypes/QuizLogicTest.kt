package com.underthykilt.poketypes

import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.PokemonType
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.QuizQuestion
import com.underthykilt.poketypes.data.generateQuestion
import com.underthykilt.poketypes.data.getEffectiveness
import com.underthykilt.poketypes.data.performanceMessage
import com.underthykilt.poketypes.data.questionKey
import com.underthykilt.poketypes.data.QUIZ_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizLogicTest {

    @Test
    fun double_mode_generates_different_defending_types() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.DOUBLE)
            assertNotEquals(q.defendingType, q.defendingType2)
        }
    }

    @Test
    fun single_mode_has_no_second_defending_type() {
        repeat(20) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.SINGLE)
            assertNull(q.defendingType2)
        }
    }

    @Test
    fun performance_message_perfect() {
        assertEquals("Perfect!", performanceMessage(QUIZ_LENGTH))
    }

    @Test
    fun performance_message_outstanding() {
        assertEquals("Outstanding!", performanceMessage(9))
    }

    @Test
    fun performance_message_great_job() {
        assertEquals("Great job!", performanceMessage(7))
    }

    @Test
    fun performance_message_keep_it_up() {
        assertEquals("Keep it up!", performanceMessage(5))
    }

    @Test
    fun performance_message_keep_practicing() {
        assertEquals("Keep practicing!", performanceMessage(3))
    }

    @Test
    fun performance_message_study_the_chart() {
        assertEquals("Study the chart!", performanceMessage(0))
    }

    @Test
    fun performance_message_boundaries_8_is_great() {
        assertEquals("Great job!", performanceMessage(8))
    }

    @Test
    fun performance_message_boundaries_6_is_keep_it_up() {
        assertEquals("Keep it up!", performanceMessage(6))
    }

    // --- Reverse Single ---

    @Test
    fun reverse_single_has_four_answer_choices() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            assertEquals(4, q.answerChoices.size)
        }
    }

    @Test
    fun reverse_single_correct_index_is_in_range() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            assertTrue(q.correctAnswer.toInt() in 0..3)
        }
    }

    @Test
    fun reverse_single_correct_choice_effectiveness_matches_prompt_multiplier() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            val (correctType, _) = q.answerChoices[q.correctAnswer.toInt()]
            val actual = getEffectiveness(q.attackingType, correctType, Generation.GEN6_PLUS)
            assertEquals(q.promptMultiplier, actual)
        }
    }

    @Test
    fun reverse_single_wrong_choices_dont_match_prompt_multiplier() {
        repeat(30) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            q.answerChoices.forEachIndexed { i, (type, _) ->
                if (i != q.correctAnswer.toInt()) {
                    val actual = getEffectiveness(q.attackingType, type, Generation.GEN6_PLUS)
                    assertNotEquals("Choice $i effectiveness should not match prompt", q.promptMultiplier, actual)
                }
            }
        }
    }

    @Test
    fun reverse_single_choices_are_distinct() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            assertEquals(q.answerChoices.size, q.answerChoices.toSet().size)
        }
    }

    @Test
    fun reverse_single_answer_choice_multipliers_match_actual_effectiveness() {
        repeat(30) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            q.answerChoices.forEachIndexed { i, (type, _) ->
                val expected = getEffectiveness(q.attackingType, type, Generation.GEN6_PLUS)
                assertEquals(expected, q.answerChoiceMultipliers[i])
            }
        }
    }

    @Test
    fun reverse_single_answer_choices_have_no_second_type() {
        repeat(30) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_SINGLE)
            q.answerChoices.forEach { (_, type2) -> assertNull(type2) }
        }
    }

    // --- Reverse Double ---

    @Test
    fun reverse_double_has_four_answer_choices() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE)
            assertEquals(4, q.answerChoices.size)
        }
    }

    @Test
    fun reverse_double_correct_index_is_in_range() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE)
            assertTrue(q.correctAnswer.toInt() in 0..3)
        }
    }

    @Test
    fun reverse_double_correct_choice_effectiveness_matches_prompt_multiplier() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE)
            val (t1, t2) = q.answerChoices[q.correctAnswer.toInt()]
            val actual = getEffectiveness(q.attackingType, t1, Generation.GEN6_PLUS) *
                    (if (t2 != null) getEffectiveness(q.attackingType, t2, Generation.GEN6_PLUS) else 1f)
            assertEquals(q.promptMultiplier, actual)
        }
    }

    @Test
    fun reverse_double_correct_pair_matches_defending_types() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE)
            val (t1, t2) = q.answerChoices[q.correctAnswer.toInt()]
            val defPair = setOf(q.defendingType, q.defendingType2)
            assertEquals(defPair, setOfNotNull(t1, t2))
        }
    }

    @Test
    fun reverse_double_answer_choices_have_distinct_pairs() {
        repeat(50) {
            val q = generateQuestion(Generation.GEN6_PLUS, QuizMode.REVERSE_DOUBLE)
            val normalized = q.answerChoices.map { (t1, t2) ->
                if (t2 == null) setOf(t1) else setOf(t1, t2)
            }
            assertEquals(normalized.size, normalized.toSet().size)
        }
    }

    // --- questionKey ---

    @Test
    fun question_key_double_mode_is_order_independent() {
        val q1 = QuizQuestion(PokemonType.FIRE, PokemonType.GRASS, PokemonType.WATER, 1f)
        val q2 = QuizQuestion(PokemonType.FIRE, PokemonType.WATER, PokemonType.GRASS, 1f)
        assertEquals(questionKey(q1), questionKey(q2))
    }

    @Test
    fun question_key_single_mode_uses_both_types() {
        val q1 = QuizQuestion(PokemonType.FIRE, PokemonType.GRASS, null, 2f)
        val q2 = QuizQuestion(PokemonType.GRASS, PokemonType.FIRE, null, 0.5f)
        assertNotEquals(questionKey(q1), questionKey(q2))
    }
}
