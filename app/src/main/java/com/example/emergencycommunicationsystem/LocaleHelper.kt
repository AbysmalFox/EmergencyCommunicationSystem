package com.example.emergencycommunicationsystem

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Updates the app locale for activities (used during activity recreation)
     */
    fun setAppLocale(context: Context, langCode: String): Context {
        val locale = Locale.Builder().setLanguage(langCode).build()
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Creates a locale-aware context without modifying the base context
     */
    fun createLocaleContext(context: Context, langCode: String): Context {
        val locale = Locale.Builder().setLanguage(langCode).build()
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Gets the locale from language code
     */
    fun getLocaleFromCode(langCode: String): Locale {
        return when (langCode) {
            "fil" -> Locale("fil")
            "es" -> Locale("es")
            else -> Locale("en")
        }
    }
}
