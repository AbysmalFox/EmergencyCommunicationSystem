package com.example.emergencycommunicationsystem.util

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Free on-device translation service using Google ML Kit
 * Works offline after initial model download
 * No API keys required - completely free
 */
object TranslationService {
    private const val TAG = "TranslationService"
    
    // Cache of translator instances to avoid recreating them
    private val translators = mutableMapOf<String, Translator>()
    
    // Cache of translations to avoid re-translating the same text
    private val translationCache = mutableMapOf<String, String>()
    
    /**
     * Translate text from source language to target language
     * @param text Text to translate
     * @param targetLanguage Target language code (e.g., "fil", "es", "en")
     * @param sourceLanguage Source language code (default: "en")
     * @return Translated text or original text if translation fails
     */
    suspend fun translate(
        text: String,
        targetLanguage: String,
        sourceLanguage: String = "en"
    ): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text
        if (targetLanguage == sourceLanguage || targetLanguage == "en") {
            return@withContext text // No translation needed
        }
        
        // Check cache first
        val cacheKey = "${text}_${sourceLanguage}_${targetLanguage}"
        translationCache[cacheKey]?.let {
            LogFilter.d(TAG, "Translation cache hit for: ${text.take(20)}...")
            return@withContext it
        }
        
        try {
            // Get or create translator
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguage)
            
            // Download model if needed (happens automatically on first use)
            val downloadTask = translator.downloadModelIfNeeded()
            downloadTask.await()
            
            if (!downloadTask.isSuccessful) {
                LogFilter.w(TAG, "Failed to download translation model")
                return@withContext text // Return original on failure
            }
            
            // Translate text
            val translatedText = translator.translate(text).await()
            
            // Cache the translation (limit cache size to avoid memory issues)
            if (translationCache.size < 1000) { // Limit to 1000 cached translations
                translationCache[cacheKey] = translatedText
            }
            
            LogFilter.d(TAG, "Translated: ${text.take(20)}... -> ${translatedText.take(20)}...")
            
            translatedText
        } catch (e: Exception) {
            LogFilter.e(TAG, "Translation failed: ${e.message}", e)
            text // Return original text on error
        }
    }
    
    /**
     * Translate multiple texts in batch (more efficient)
     */
    suspend fun translateBatch(
        texts: List<String>,
        targetLanguage: String,
        sourceLanguage: String = "en"
    ): List<String> = withContext(Dispatchers.IO) {
        if (targetLanguage == sourceLanguage || targetLanguage == "en") {
            return@withContext texts
        }
        
        texts.map { text ->
            translate(text, targetLanguage, sourceLanguage)
        }
    }
    
    /**
     * Get or create a translator instance for the language pair
     */
    private suspend fun getOrCreateTranslator(
        sourceLanguage: String,
        targetLanguage: String
    ): Translator = withContext(Dispatchers.IO) {
        val key = "${sourceLanguage}_${targetLanguage}"
        
        translators.getOrPut(key) {
            val sourceLang = getMLKitLanguage(sourceLanguage)
            val targetLang = getMLKitLanguage(targetLanguage)
            
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            
            Translation.getClient(options)
        }
    }
    
    /**
     * Convert language code to ML Kit TranslateLanguage
     */
    private fun getMLKitLanguage(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "fil", "tl" -> {
                // ML Kit uses "tl" for Tagalog/Filipino
                try {
                    TranslateLanguage.fromLanguageTag("tl") ?: TranslateLanguage.ENGLISH
                } catch (e: Exception) {
                    TranslateLanguage.ENGLISH
                }
            }
            "es" -> TranslateLanguage.SPANISH
            "en" -> TranslateLanguage.ENGLISH
            "ceb" -> {
                // Cebuano might not be directly supported, try language tag
                try {
                    TranslateLanguage.fromLanguageTag("ceb") ?: TranslateLanguage.ENGLISH
                } catch (e: Exception) {
                    TranslateLanguage.ENGLISH
                }
            }
            else -> {
                // Try to get language directly, fallback to English
                try {
                    TranslateLanguage.fromLanguageTag(languageCode) ?: TranslateLanguage.ENGLISH
                } catch (e: Exception) {
                    TranslateLanguage.ENGLISH
                }
            }
        }
    }
    
    /**
     * Clear translation cache (useful if memory is an issue)
     */
    fun clearCache() {
        translationCache.clear()
        LogFilter.d(TAG, "Translation cache cleared")
    }
    
    /**
     * Close all translators (call when app is closing)
     */
    suspend fun closeTranslators() = withContext(Dispatchers.IO) {
        translators.values.forEach { translator ->
            try {
                translator.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
        translators.clear()
    }
}
