package com.example.emergencycommunicationsystem.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

object UserPrefs {
    private val Context.dataStore by preferencesDataStore("settings")

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val MAGNIFIER_ENABLED_KEY = booleanPreferencesKey("magnifier_enabled")

    suspend fun saveLanguage(context: Context, langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = langCode
        }
    }

    suspend fun saveTheme(context: Context, theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    suspend fun saveMagnifierEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MAGNIFIER_ENABLED_KEY] = enabled
        }
    }

    fun getLanguage(context: Context): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: when (Locale.getDefault().language) {
                "es" -> "es"
                "fil", "tl" -> "fil"
                else -> "en"
            }
        }

    fun getTheme(context: Context): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: "light" // Default to light mode instead of system
        }

    fun isMagnifierEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[MAGNIFIER_ENABLED_KEY] ?: false
        }
}