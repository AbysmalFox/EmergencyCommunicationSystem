package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.emergencycommunicationsystem.data.UserPrefs
import java.util.Locale

/**
 * Locale-aware context that automatically updates when language changes
 */
val LocalLocaleContext = compositionLocalOf<Context> {
    error("No LocaleContext provided")
}

/**
 * Manages application locale and provides reactive locale updates
 */
object LocaleManager {
    private var _currentLocale: Locale = Locale.getDefault()
    val currentLocale: Locale get() = _currentLocale

    fun updateLocale(locale: Locale) {
        _currentLocale = locale
        Locale.setDefault(locale)
    }

    fun getLocaleFromCode(langCode: String): Locale {
        return when (langCode) {
            "fil" -> Locale("fil")
            "es" -> Locale("es")
            "bcl" -> Locale("bcl")
            "ceb" -> Locale("ceb")
            "war" -> Locale("war")
            "ilo" -> Locale("ilo")
            "en" -> Locale("en")
            else -> Locale(langCode)
        }
    }
}

/**
 * Creates a locale-aware context wrapper
 */
fun Context.createLocaleContext(locale: Locale): Context {
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

/**
 * Composable that provides locale-aware context to all child composables
 */
@Composable
fun LocaleProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by UserPrefs.getLanguage(context).collectAsState(initial = "en")
    
    // Update LocaleManager with current language
    val locale = remember(currentLanguage) {
        LocaleManager.getLocaleFromCode(currentLanguage).also {
            LocaleManager.updateLocale(it)
        }
    }
    
    // Create locale-aware context
    val localeContext = remember(locale) {
        context.createLocaleContext(locale)
    }
    
    // Provide the locale-aware context to all children
    CompositionLocalProvider(LocalLocaleContext provides localeContext) {
        content()
    }
}

/**
 * Get locale-aware context from composition local
 */
@Composable
fun getLocaleContext(): Context = LocalLocaleContext.current

@Composable
fun localizedStringResource(id: Int): String {
    return LocalLocaleContext.current.getString(id)
}

@Composable
fun localizedStringResource(id: Int, vararg args: Any): String {
    return LocalLocaleContext.current.getString(id, *args)
}
