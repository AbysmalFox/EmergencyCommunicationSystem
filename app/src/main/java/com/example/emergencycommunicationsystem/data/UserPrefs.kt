package com.example.emergencycommunicationsystem.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

object UserPrefs {
    private val Context.dataStore by preferencesDataStore("settings")
    private const val LEGACY_PREFS_NAME = "settings_sync"
    private const val LEGACY_LANGUAGE_KEY = "app_language"

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    private val THEME_KEY = stringPreferencesKey("app_theme")
    private val MAGNIFIER_ENABLED_KEY = booleanPreferencesKey("magnifier_enabled")
    private val ALERTS_COMPACT_MODE_KEY = booleanPreferencesKey("alerts_compact_mode")
    private val ALERTS_LAST_SYNC_MILLIS_KEY = longPreferencesKey("alerts_last_sync_millis")
    private val MAP_WEATHER_LAST_SYNC_MILLIS_KEY = longPreferencesKey("map_weather_last_sync_millis")

    suspend fun saveLanguage(context: Context, langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = langCode
        }
        // Keep a synchronous copy for early Activity locale setup (attachBaseContext).
        context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LEGACY_LANGUAGE_KEY, langCode)
            .apply()
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

    suspend fun saveAlertsCompactMode(context: Context, isCompactMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ALERTS_COMPACT_MODE_KEY] = isCompactMode
        }
    }

    suspend fun saveAlertsLastSyncMillis(context: Context, millis: Long) {
        context.dataStore.edit { prefs ->
            prefs[ALERTS_LAST_SYNC_MILLIS_KEY] = millis
        }
    }

    suspend fun saveMapWeatherLastSyncMillis(context: Context, millis: Long) {
        context.dataStore.edit { prefs ->
            prefs[MAP_WEATHER_LAST_SYNC_MILLIS_KEY] = millis
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

    fun getLanguageSync(context: Context): String {
        val stored = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LEGACY_LANGUAGE_KEY, null)
        if (!stored.isNullOrBlank()) return stored

        return when (Locale.getDefault().language) {
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

    fun isAlertsCompactMode(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[ALERTS_COMPACT_MODE_KEY] ?: false
        }

    fun getAlertsLastSyncMillis(context: Context): Flow<Long> =
        context.dataStore.data.map { prefs ->
            prefs[ALERTS_LAST_SYNC_MILLIS_KEY] ?: 0L
        }

    fun getMapWeatherLastSyncMillis(context: Context): Flow<Long> =
        context.dataStore.data.map { prefs ->
            prefs[MAP_WEATHER_LAST_SYNC_MILLIS_KEY] ?: 0L
        }
}
