package com.underthykilt.poketypes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme")
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

class ThemeRepository(private val context: Context) {
    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: true }

    suspend fun setDarkTheme(dark: Boolean) {
        context.themeDataStore.edit { prefs -> prefs[DARK_MODE_KEY] = dark }
    }
}
