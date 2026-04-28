package com.underthykilt.poketypes.data

import android.content.Context

const val QUIZ_LENGTH = 10

object ScoreHistory {
    private const val PREFS = "score_history"

    fun save(context: Context, mode: String, score: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val scores = load(context, mode).toMutableList()
        scores.add(score)
        if (scores.size > QUIZ_LENGTH) scores.removeAt(0)
        prefs.edit().putString(mode, scores.joinToString(",")).apply()
    }

    fun load(context: Context, mode: String): List<Int> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(mode, "") ?: return emptyList()
        return if (raw.isBlank()) emptyList()
        else raw.split(",").mapNotNull { it.toIntOrNull() }
    }
}
