package com.example.emergencycommunicationsystem.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

object UserPrefs {
    private val Context.dataStore by preferencesDataStore("settings")

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    suspend fun saveLanguage(context: Context, langCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = langCode
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
}
