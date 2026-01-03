package com.example.emergencycommunicationsystem

import android.content.Context
import java.util.Locale

object LocaleHelper {

    fun setAppLocale(context: Context, langCode: String): Context {
        val locale = Locale.Builder().setLanguage(langCode).build()
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
