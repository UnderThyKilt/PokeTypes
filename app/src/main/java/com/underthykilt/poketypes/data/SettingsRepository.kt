package com.underthykilt.poketypes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val KEY_DARK_THEME  = booleanPreferencesKey("dark_theme")
private val KEY_GENERATION  = stringPreferencesKey("generation")
private val KEY_DIFFICULTY  = stringPreferencesKey("difficulty")
private val KEY_QUIZ_LENGTH = stringPreferencesKey("quiz_length")

data class AppSettings(
    val isDarkTheme: Boolean  = true,
    val generation: Generation = Generation.GEN6_PLUS,
    val difficulty: Difficulty = Difficulty.HARD,
    val quizLength: QuizLength = QuizLength.TEN,
)

class SettingsRepository(private val context: Context) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            isDarkTheme = prefs[KEY_DARK_THEME] ?: true,
            generation  = prefs[KEY_GENERATION]?.let  { runCatching { Generation.valueOf(it)  }.getOrNull() } ?: Generation.GEN6_PLUS,
            difficulty  = prefs[KEY_DIFFICULTY]?.let  { runCatching { Difficulty.valueOf(it)  }.getOrNull() } ?: Difficulty.HARD,
            quizLength  = prefs[KEY_QUIZ_LENGTH]?.let { runCatching { QuizLength.valueOf(it)  }.getOrNull() } ?: QuizLength.TEN,
        )
    }

    suspend fun setDarkTheme(dark: Boolean)      { context.settingsDataStore.edit { it[KEY_DARK_THEME]  = dark       } }
    suspend fun setGeneration(gen: Generation)    { context.settingsDataStore.edit { it[KEY_GENERATION]  = gen.name   } }
    suspend fun setDifficulty(diff: Difficulty)   { context.settingsDataStore.edit { it[KEY_DIFFICULTY]  = diff.name  } }
    suspend fun setQuizLength(len: QuizLength)    { context.settingsDataStore.edit { it[KEY_QUIZ_LENGTH] = len.name   } }
}
