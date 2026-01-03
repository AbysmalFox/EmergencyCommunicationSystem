package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.SettingsApiService
import com.example.emergencycommunicationsystem.network.UserPreferences

class SettingsRepository {
    private val apiService: SettingsApiService = ApiClient.settingsApiService

    suspend fun getUserPreferences(userId: Int): UserPreferences {
        val response = apiService.getUserPreferences(userId)
        if (response.success) {
            return response.preferences
        } else {
            throw Exception("Failed to fetch preferences from API.")
        }
    }

    suspend fun updateUserPreferences(preferences: UserPreferences) {
        val response = apiService.updateUserPreferences(preferences)
        if (!response.success) {
            // Use the message from the server if available
            throw Exception(response.message ?: "Failed to update preferences on the server.")
        }
    }
}