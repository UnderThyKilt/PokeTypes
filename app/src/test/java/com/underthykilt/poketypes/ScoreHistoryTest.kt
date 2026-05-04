package com.underthykilt.poketypes

import com.underthykilt.poketypes.data.QUIZ_LENGTH
import com.underthykilt.poketypes.data.appendScore
import com.underthykilt.poketypes.data.parseScores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreHistoryTest {

    @Test
    fun parseScores_empty_string_returns_empty_list() {
        assertEquals(emptyList<Int>(), parseScores(""))
    }

    @Test
    fun parseScores_blank_string_returns_empty_list() {
        assertEquals(emptyList<Int>(), parseScores("   "))
    }

    @Test
    fun parseScores_valid_csv_returns_list() {
        assertEquals(listOf(5, 8, 10), parseScores("5,8,10"))
    }

    @Test
    fun parseScores_malformed_values_are_skipped() {
        assertEquals(listOf(5, 10), parseScores("5,abc,10"))
    }

    @Test
    fun parseScores_all_malformed_returns_empty() {
        assertEquals(emptyList<Int>(), parseScores("x,y,z"))
    }

    @Test
    fun appendScore_adds_to_empty_list() {
        assertEquals(listOf(7), appendScore(emptyList(), 7, QUIZ_LENGTH))
    }

    @Test
    fun appendScore_appends_to_existing() {
        val result = appendScore(listOf(3, 5), 8, QUIZ_LENGTH)
        assertEquals(listOf(3, 5, 8), result)
    }

    @Test
    fun appendScore_caps_at_max_size() {
        val full = List(QUIZ_LENGTH) { it }
        val result = appendScore(full, 99, QUIZ_LENGTH)
        assertEquals(QUIZ_LENGTH, result.size)
        assertEquals(99, result.last())
    }

    @Test
    fun appendScore_drops_oldest_when_full() {
        val full = List(QUIZ_LENGTH) { it }
        val result = appendScore(full, 99, QUIZ_LENGTH)
        assertTrue(1 == result.first())
    }
}
