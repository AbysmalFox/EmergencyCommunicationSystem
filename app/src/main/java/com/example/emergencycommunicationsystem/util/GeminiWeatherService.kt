package com.example.emergencycommunicationsystem.util

import android.util.Log
import com.example.emergencycommunicationsystem.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Service for generating AI-powered weather advice using Google Gemini
 * Dynamically discovers supported models and falls back to template-based advice.
 */
object GeminiWeatherService {
    private const val TAG = "GeminiWeatherService"
    
    private val responseCache = mutableMapOf<String, String>()
    private const val CACHE_EXPIRY_MS = 30 * 60 * 1000L // 30 minutes
    private val cacheTimestamps = mutableMapOf<String, Long>()
    
    private var lastUsedModel: String? = null
    
    // DEBUG MODE: Set to false to bypass cache and add [AI] prefix
    private const val DEBUG_MODE = false // Turning off for production feel, can turn back on if needed
    
    /**
     * Generate weather advice using Gemini AI with template fallback
     */
    suspend fun getWeatherAdvice(
        condition: String,
        temp: Double,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        visibility: Int,
        location: String = "Quezon City, Philippines",
        language: String = "en",
        templateFallback: () -> String
    ): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Requesting detailed weather advice for $location in $language")
        
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            Log.e(TAG, "GEMINI_API_KEY is BLANK")
            return@withContext templateFallback()
        }
        
        // Cache key should include language
        val cacheKey = "${condition}_${temp.toInt()}_${humidity}_${windSpeed.toInt()}_${visibility / 1000}_$language"
        
        if (!DEBUG_MODE) {
            val cachedResponse = responseCache[cacheKey]
            val cacheTime = cacheTimestamps[cacheKey] ?: 0L
            if (cachedResponse != null && (System.currentTimeMillis() - cacheTime) < CACHE_EXPIRY_MS) {
                return@withContext cachedResponse
            }
        }
        
        try {
            val aiResponse = generateAIAdvice(
                condition = condition,
                temp = temp,
                feelsLike = feelsLike,
                humidity = humidity,
                windSpeed = windSpeed,
                visibility = visibility,
                location = location,
                language = language
            )
            
            val finalResponse = if (DEBUG_MODE) "[AI Bot] $aiResponse" else aiResponse
            
            responseCache[cacheKey] = finalResponse
            cacheTimestamps[cacheKey] = System.currentTimeMillis()
            
            finalResponse
        } catch (e: Exception) {
            Log.e(TAG, "AI advice unavailable", e)
            val errorDisplay = if (DEBUG_MODE) "[AI ERR: ${e.message}] " else ""
            errorDisplay + templateFallback()
        }
    }

    /**
     * Fetches the list of models available for the current API key and finds one
     * that supports 'generateContent'.
     */
    private suspend fun discoverSupportedModel(): String = withContext(Dispatchers.IO) {
        lastUsedModel?.let { return@withContext it }

        Log.d(TAG, "Discovering supported models...")
        val client = OkHttpClient()
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=${BuildConfig.GEMINI_API_KEY.trim()}"
        val request = Request.Builder().url(url).build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Could not list models")
        
        if (!response.isSuccessful) throw Exception("List models failed: ${response.code}")

        val json = JSONObject(responseBody)
        val models = json.getJSONArray("models")
        
        val priorityModels = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-pro")
        
        val supportedModels = mutableListOf<String>()
        for (i in 0 until models.length()) {
            val model = models.getJSONObject(i)
            val name = model.getString("name").substringAfter("models/")
            val methods = model.getJSONArray("supportedGenerationMethods")
            
            var supportsGenerate = false
            for (j in 0 until methods.length()) {
                if (methods.getString(j) == "generateContent") {
                    supportsGenerate = true
                    break
                }
            }
            
            if (supportsGenerate) {
                supportedModels.add(name)
            }
        }

        val selectedModel = priorityModels.firstOrNull { it in supportedModels } 
            ?: supportedModels.firstOrNull() 
            ?: throw Exception("No models support generateContent")

        lastUsedModel = selectedModel
        selectedModel
    }
    
    private suspend fun callGeminiAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val model = discoverSupportedModel()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
        
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }.toString()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=${BuildConfig.GEMINI_API_KEY.trim()}"
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            val msg = JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            if (response.code == 404) lastUsedModel = null
            throw Exception(msg)
        }
        
        val json = JSONObject(responseBody)
        val text = json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        
        text.trim()
    }

    private suspend fun generateAIAdvice(
        condition: String,
        temp: Double,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        visibility: Int,
        location: String,
        language: String
    ): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        // Map language code to full name for clearer prompt
        val languageName = when(language) {
            "fil", "tl" -> "Tagalog/Filipino"
            "es" -> "Spanish"
            "ceb" -> "Cebuano"
            "war" -> "Waray"
            "bcl" -> "Bicolano"
            else -> "English"
        }
        
        val prompt = """
            You are a helpful, professional weather assistant for $location.
            
            Based on these specific conditions, provide conversational advice including a tip and any necessary warnings:
            - Condition: $condition
            - Temp: ${temp.toInt()}°C (Feels like ${feelsLike.toInt()}°C)
            - Humidity: $humidity%
            - Wind: $windSpeed km/h
            - Visibility: ${visibility / 1000} km
            - Time: $timeOfDay
            
            Guidelines:
            - Provide 2-3 sentences.
            - Be practical and actionable (what to wear, what to bring, safety precautions).
            - Mention specific details from the data if they are notable (e.g., high humidity or wind).
            - Use a friendly but informative tone.
            - Max 250 characters.
            - IMPORTANT: Respond strictly in $languageName.
            
            Respond only with the advice.
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }

    fun clearCache() {
        responseCache.clear()
        cacheTimestamps.clear()
        lastUsedModel = null
    }
}
