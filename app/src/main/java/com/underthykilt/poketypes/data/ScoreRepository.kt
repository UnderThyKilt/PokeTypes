package com.underthykilt.poketypes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ScoreRepository {
    fun loadScores(mode: String): Flow<List<Int>>
    suspend fun saveScore(mode: String, score: Int)
}

internal fun parseScores(raw: String): List<Int> =
    if (raw.isBlank()) emptyList()
    else raw.split(",").mapNotNull { it.toIntOrNull() }

internal fun appendScore(scores: List<Int>, score: Int, maxSize: Int): List<Int> =
    (scores + score).let { if (it.size > maxSize) it.drop(1) else it }

private val Context.scoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "score_history")

class DataStoreScoreRepository(private val context: Context) : ScoreRepository {
    override fun loadScores(mode: String): Flow<List<Int>> {
        val key = stringPreferencesKey(mode)
        return context.scoreDataStore.data.map { prefs -> parseScores(prefs[key] ?: "") }
    }

    override suspend fun saveScore(mode: String, score: Int) {
        val key = stringPreferencesKey(mode)
        context.scoreDataStore.edit { prefs ->
            prefs[key] = appendScore(parseScores(prefs[key] ?: ""), score, QUIZ_LENGTH).joinToString(",")
        }
    }
}
