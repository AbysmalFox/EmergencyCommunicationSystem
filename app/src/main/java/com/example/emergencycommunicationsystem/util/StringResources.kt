package com.example.emergencycommunicationsystem.util

import androidx.compose.runtime.Composable

/**
 * Utility functions for accessing localized string resources
 * These functions automatically use the locale-aware context provided by LocaleProvider
 */

/**
 * Get a localized string resource using the locale-aware context
 * This should be used instead of stringResource() when you need locale-aware strings
 * that update dynamically when language changes
 */
@Composable
fun localizedString(resId: Int): String {
    val context = getLocaleContext()
    return context.getString(resId)
}

/**
 * Get a localized string resource with format arguments using the locale-aware context
 */
@Composable
fun localizedString(resId: Int, vararg formatArgs: Any): String {
    val context = getLocaleContext()
    return context.getString(resId, *formatArgs)
}

/**
 * Get a localized string array using the locale-aware context
 */
@Composable
fun localizedStringArray(resId: Int): Array<String> {
    val context = getLocaleContext()
    return context.resources.getStringArray(resId)
}
