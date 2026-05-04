package com.underthykilt.poketypes

import com.underthykilt.poketypes.data.Generation
import com.underthykilt.poketypes.data.QuizMode
import com.underthykilt.poketypes.data.generateQuestion
import com.underthykilt.poketypes.data.performanceMessage
import com.underthykilt.poketypes.data.QUIZ_LENGTH
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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
}
