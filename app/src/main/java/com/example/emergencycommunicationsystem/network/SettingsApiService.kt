package com.example.emergencycommunicationsystem.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Data class to match the JSON response from get_preferences.php
data class UserPreferences(
    val receive_notifications: Boolean,
    val crime_alerts: Boolean,
    val disaster_warnings: Boolean,
    val fire_alerts: Boolean,
    val weather_advisories: Boolean
)

data class PreferencesResponse(val success: Boolean, val preferences: UserPreferences)
data class UpdatePreferencesResponse(val success: Boolean, val message: String)

interface SettingsApiService {
    @GET("user/get_preferences.php")
    suspend fun getUserPreferences(@Query("user_id") userId: Int): PreferencesResponse

    @POST("user/update_preferences.php")
    suspend fun updateUserPreferences(@Body preferences: UserPreferences): UpdatePreferencesResponse
}
