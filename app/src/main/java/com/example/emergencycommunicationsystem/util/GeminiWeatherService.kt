package com.example.emergencycommunicationsystem.util

import com.example.emergencycommunicationsystem.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Service for generating AI-powered weather advice using Google Gemini
 * Falls back to template-based advice if AI is unavailable
 */
object GeminiWeatherService {
    private const val TAG = "GeminiWeatherService"
    
    // Cache AI responses to avoid excessive API calls
    private val responseCache = mutableMapOf<String, String>()
    private const val CACHE_EXPIRY_MS = 30 * 60 * 1000L // 30 minutes
    private val cacheTimestamps = mutableMapOf<String, Long>()
    
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
        templateFallback: () -> String
    ): String = withContext(Dispatchers.IO) {
        // Check if API key is configured
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            LogFilter.d(TAG, "Gemini API key not configured, using template fallback")
            return@withContext templateFallback()
        }
        
        // Create cache key
        val cacheKey = "${condition}_${temp.toInt()}_${humidity}_${windSpeed.toInt()}_${visibility / 1000}"
        
        // Check cache first
        val cachedResponse = responseCache[cacheKey]
        val cacheTime = cacheTimestamps[cacheKey] ?: 0L
        if (cachedResponse != null && (System.currentTimeMillis() - cacheTime) < CACHE_EXPIRY_MS) {
            LogFilter.d(TAG, "Using cached AI response")
            return@withContext cachedResponse
        }
        
        try {
            // Generate AI response
            val aiResponse = generateAIAdvice(
                condition = condition,
                temp = temp,
                feelsLike = feelsLike,
                humidity = humidity,
                windSpeed = windSpeed,
                visibility = visibility,
                location = location
            )
            
            // Cache the response
            responseCache[cacheKey] = aiResponse
            cacheTimestamps[cacheKey] = System.currentTimeMillis()
            
            // Limit cache size
            if (responseCache.size > 50) {
                val oldestKey = cacheTimestamps.minByOrNull { it.value }?.key
                oldestKey?.let {
                    responseCache.remove(it)
                    cacheTimestamps.remove(it)
                }
            }
            
            LogFilter.d(TAG, "AI response generated successfully")
            aiResponse
        } catch (e: Exception) {
            LogFilter.w(TAG, "AI generation failed: ${e.message}, using template fallback", e)
            templateFallback()
        }
    }
    
    /**
     * Call Gemini API via HTTP
     */
    private suspend fun callGeminiAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
        
        val requestBody = JSONObject().apply {
            put("contents", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", prompt)
                    })
                })
            })
        }.toString()
        
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${BuildConfig.GEMINI_API_KEY}")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            throw Exception("API error: ${response.code} - $responseBody")
        }
        
        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.getJSONArray("candidates")
        if (candidates.length() == 0) {
            throw Exception("No candidates in response")
        }
        
        val candidate = candidates.getJSONObject(0)
        val content = candidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val textPart = parts.getJSONObject(0)
        
        textPart.getString("text").trim()
    }
    
    /**
     * Generate weather advice using Gemini AI
     */
    private suspend fun generateAIAdvice(
        condition: String,
        temp: Double,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        visibility: Int,
        location: String
    ): String = withContext(Dispatchers.IO) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
        
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
        val isTyphoonSeason = month in 6..11 // June to November in Philippines
        
        val prompt = """
            You are a friendly, helpful weather assistant for $location.
            
            Provide a brief, conversational weather advice (1-2 sentences, max 150 characters) based on:
            - Weather condition: $condition
            - Temperature: ${String.format("%.1f", temp)}°C (feels like ${String.format("%.1f", feelsLike)}°C)
            - Humidity: $humidity%
            - Wind speed: ${String.format("%.1f", windSpeed)} km/h
            - Visibility: ${visibility / 1000} km
            - Time of day: $timeOfDay
            - Month: $month (${if (isTyphoonSeason) "typhoon season" else "not typhoon season"})
            
            Guidelines:
            - Be conversational and friendly (like a helpful friend)
            - Keep it practical and actionable
            - Use emojis sparingly (1-2 max) if appropriate
            - Consider local context (Philippines weather patterns, typhoon season, etc.)
            - If severe weather, add appropriate warnings
            - Don't be too technical, keep it simple
            - Make it feel natural, not robotic
            
            Respond ONLY with the weather advice, nothing else.
        """.trimIndent()
        
        try {
            val advice = callGeminiAPI(prompt)
            
            // Validate response length
            if (advice.length > 200) {
                // Truncate if too long
                advice.substring(0, 197) + "..."
            } else {
                advice
            }
        } catch (e: Exception) {
            LogFilter.e(TAG, "Gemini API error: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Clear the response cache
     */
    fun clearCache() {
        responseCache.clear()
        cacheTimestamps.clear()
        LogFilter.d(TAG, "AI response cache cleared")
    }
}
