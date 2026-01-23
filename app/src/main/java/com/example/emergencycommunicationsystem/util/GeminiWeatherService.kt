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
        
        // Include condition and temp in cache key but ignore minute variations
        val cacheKey = "${condition}_${temp.toInt()}_${humidity}_${language}"
        
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
            
            finalResponse
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
            "fil", "tl" -> "Tagalog/Filipino (e.g., 'Mainit ang panahon today')"
            "es" -> "Spanish (e.g., 'El clima está caluroso')"
            "ceb" -> "Cebuano/Bisaya (e.g., 'Init kaayo ang panahon karon')"
            "war" -> "Waray-Waray (the Samar-Leyte language, e.g., 'Mapaso an panahon yana')"
            "ilo" -> "Ilocano (e.g., 'Napudot ti tiempo ita')"
            "bcl" -> "Bicolano (e.g., 'Mainit an panahon ngunyan')"
            else -> "English"
        }
        
        val prompt = """
            You are a friendly, talkative local weather reporter for $location. 
            
            REAL-TIME WEATHER DATA:
            - Location: $location
            - Current Sky: $condition
            - Temperature: ${temp.toInt()}°C (Feels like ${feelsLike.toInt()}°C)
            - Humidity: $humidity%
            - Wind Speed: $windSpeed km/h
            - Visibility: ${visibility / 1000} km
            - Time: $timeOfDay
            
            HOURLY FORECAST FOR THE NEXT FEW HOURS:
            $forecastInfo
            
            YOUR TASK:
            Write a detailed, helpful, and community-oriented weather update in the $languageHint language.
            
            STRUCTURE YOUR RESPONSE:
            1. A warm local greeting in $languageHint.
            2. Describe current conditions using ALL details (feels like, humidity, wind, and visibility).
            3. Mention the forecast (e.g., "Expect rain later this afternoon" or "It will stay clear until evening").
            4. Practical advice on clothing, travel, or hydration based on these specific details.
            5. A caring safety reminder for the people in $location.
            
            CONSTRAINTS:
            - USE ONLY the $languageHint language. No English words.
            - LENGTH: Be descriptive. Write at least 4-5 long sentences. 
            - TONE: Professional but friendly, like a radio broadcaster.
            
            Output: (Respond only in $languageHint)
        """.trimIndent()
        
        return callGeminiAPI(prompt)
    }

    fun clearCache() {
        responseCache.clear()
        cacheTimestamps.clear()
        lastUsedModel = null
    }
}
