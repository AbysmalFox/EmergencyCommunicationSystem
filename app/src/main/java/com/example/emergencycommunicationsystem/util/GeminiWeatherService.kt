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
    private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // Reduced to 5 minutes for more variety
    private val cacheTimestamps = mutableMapOf<String, Long>()
    
    private var lastUsedModel: String? = "gemini-1.5-flash"
    
    // DEBUG MODE: Set to true to add [AI] prefix to responses
    private const val DEBUG_MODE = false 

    suspend fun getWeatherAdvice(
        condition: String,
        temp: Double,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        visibility: Int,
        location: String = "Quezon City, Philippines",
        language: String = "en",
        forecastInfo: String = "",
        templateFallback: () -> String
    ): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "getWeatherAdvice called: Language=$language")
        
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return@withContext templateFallback()
        }
        
        // Use a more granular cache key to allow for subtle changes
        val cacheKey = "${condition}_${temp}_${humidity}_${language}_${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}"
        
        val cachedResponse = responseCache[cacheKey]
        val cacheTime = cacheTimestamps[cacheKey] ?: 0L
        if (cachedResponse != null && (System.currentTimeMillis() - cacheTime) < CACHE_EXPIRY_MS) {
            return@withContext cachedResponse
        }
        
        try {
            val finalResponse = generateAIAdvice(
                condition = condition,
                temp = temp,
                feelsLike = feelsLike,
                humidity = humidity,
                windSpeed = windSpeed,
                visibility = visibility,
                location = location,
                language = language,
                forecastInfo = forecastInfo
            )
            
            responseCache[cacheKey] = finalResponse
            cacheTimestamps[cacheKey] = System.currentTimeMillis()
            
            if (DEBUG_MODE) "[AI] $finalResponse" else finalResponse
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Error: ${e.message}")
            templateFallback()
        }
    }

    private suspend fun callGeminiAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val model = "gemini-1.5-flash"
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
            // Added generationConfig to increase randomness/creativity
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.9) // Higher temperature = more creative/random
                put("topK", 40)
                put("topP", 0.95)
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
        language: String,
        forecastInfo: String
    ): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        val languageHint = when(language) {
            "fil", "tl" -> "Tagalog/Filipino (conversational, not formal)"
            "es" -> "Spanish (natural and local)"
            "ceb" -> "Cebuano/Bisaya (informal, everyday talk)"
            "war" -> "Waray-Waray (the Samar-Leyte language)"
            "ilo" -> "Ilocano (natural dialect)"
            "bcl" -> "Bicolano (conversational)"
            else -> "English (warm and friendly)"
        }

        // Randomly pick a focus to ensure variety in responses
        val focusPoint = listOf(
            "the humidity and how it feels on the skin",
            "the wind and its effect on travel",
            "the sky's appearance and visibility",
            "practical clothing and hydration advice",
            "a specific local safety tip for this time of day"
        ).random()
        
        val prompt = """
            You are a charismatic, observant local weather storyteller for $location. 
            Avoid being a generic robot. Talk like a real person who lives in $location and cares about the neighbors.

            CURRENT VIBE AT $location:
            - Sky: $condition
            - Temp: ${temp.toInt()}°C (Actually feels like ${feelsLike.toInt()}°C)
            - Humidity: $humidity% (Sticky or Dry?)
            - Wind: $windSpeed km/h
            - Visibility: ${visibility / 1000} km
            - Time: $timeOfDay
            
            HOURLY FORECAST:
            $forecastInfo
            
            INSTRUCTIONS:
            1. Language: Use $languageHint EXCLUSIVELY. No English mixed in.
            2. Personality: Be warm and conversational. Use local-style greetings.
            3. UNIQUE FOCUS: For this specific update, emphasize **$focusPoint**.
            4. Be Vivid: Instead of "It is humid," say something like "The air feels heavy today, so stay in the shade."
            5. Content: Mention the current sky, the "feels like" temp, and a quick look at the forecast.
            6. Advice: Give one specific, clever piece of advice (e.g., if it's hot, mention a specific local drink; if it's rainy, mention a common local travel issue).
            7. Safety: End with a short, sincere neighborly reminder.

            CONSTRAINTS:
            - Length: 4-6 rich, descriptive sentences.
            - NO CLICHÉS: Do not use "Stay safe," "Stay tuned," or "Have a nice day." Use something more unique.
            - NO LISTS: Write in natural paragraph form.

            Response in $languageHint:
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }

    fun clearCache() {
        responseCache.clear()
        cacheTimestamps.clear()
        lastUsedModel = null
    }
}
