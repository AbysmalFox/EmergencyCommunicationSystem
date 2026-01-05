package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.LocationUpdateRequest
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.data.UpdateSubscriptionRequest
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.SettingsApiService

class SettingsRepository {
    private val apiService: SettingsApiService = ApiClient.settingsApiService

    suspend fun updateUserLocation(userId: Int, latitude: Double, longitude: Double, address: String?, accuracy: Float?) {
        val request = LocationUpdateRequest(userId, latitude, longitude, address, accuracy)
        val response = apiService.updateUserLocation(request)
        if (!response.success) {
            throw Exception(response.message ?: "Failed to update location on the server.")
        }
    }

    suspend fun getSubscriptionSettings(userId: Int): List<SubscriptionCategory> {
        val response = apiService.getSubscriptionSettings(userId)
        if (response.success) {
            return response.data
        } else {
            throw Exception("Failed to fetch subscription settings from API.")
        }
    }

    suspend fun updateSubscription(userId: Int, categoryId: Int, isActive: Boolean) {
        val request = UpdateSubscriptionRequest(userId, categoryId, if (isActive) 1 else 0)
        val response = apiService.updateSubscription(request)
        if (!response.isSuccessful) {
            throw Exception("Failed to update subscription on the server.")
        }
    }
}