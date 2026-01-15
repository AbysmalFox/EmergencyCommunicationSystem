package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.local.AlertDao
import com.example.emergencycommunicationsystem.data.local.toDomain
import com.example.emergencycommunicationsystem.data.local.toEntity
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.TranslationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AlertsRepository(
    private val alertDao: AlertDao,
    private val context: Context
) {

    /**
     * Returns a Flow of alerts with translation support and Offline-First strategy.
     */
    fun getAlerts(userId: Int?): Flow<Resource<List<Alert>>> = flow {
        // 1. Emit Loading state
        emit(Resource.Loading)

        // Get current language preference for translation
        val currentLanguage = UserPrefs.getLanguage(context).first()

        // 2. Fetch the current cache from Room once and emit it as Success
        // This allows the user to see data IMMEDIATELY.
        val cache = alertDao.getAllAlerts().map { list -> list.map { it.toDomain() } }.first()
        if (cache.isNotEmpty()) {
            // Translate cached alerts if needed
            val translatedCache = translateAlertsIfNeeded(cache, currentLanguage)
            emit(Resource.Success(translatedCache))
        }

        try {
            // 3. Try to get fresh data from the network
            val response = ApiClient.alertsApiService().getAlerts(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val networkAlerts = response.body()?.alerts ?: emptyList()
                
                // Translate network alerts if needed
                val translatedAlerts = translateAlertsIfNeeded(networkAlerts, currentLanguage)
                
                // 4. Update the local database (store original English text)
                alertDao.clearAlerts()
                alertDao.insertAlerts(networkAlerts.map { it.toEntity() })
            }
        } catch (e: Exception) {
            // If network fails, we don't emit an Error if we already have cache.
            // This prevents the screen from showing an error page when it has data.
            if (cache.isEmpty()) {
                emit(Resource.Error("Could not connect to server and no cached data found."))
            }
        }

        // 5. Finally, observe the database flow for any future changes with translation
        emitAll(alertDao.getAllAlerts().map { list ->
            val alerts = list.map { it.toDomain() }
            val translatedAlerts = translateAlertsIfNeeded(alerts, currentLanguage)
            Resource.Success(translatedAlerts)
        })
    }
    
    /**
     * Translate alerts if language is not English
     */
    private suspend fun translateAlertsIfNeeded(
        alerts: List<Alert>,
        targetLanguage: String
    ): List<Alert> {
        if (targetLanguage == "en") {
            return alerts // No translation needed
        }
        
        return alerts.map { alert ->
            try {
                val translatedTitle = if (!alert.title.isNullOrBlank()) {
                    TranslationService.translate(alert.title, targetLanguage)
                } else {
                    alert.title
                }
                
                val translatedContent = if (!alert.content.isNullOrBlank()) {
                    TranslationService.translate(alert.content, targetLanguage)
                } else {
                    alert.content
                }
                
                val translatedLocation = if (!alert.location.isNullOrBlank()) {
                    TranslationService.translate(alert.location, targetLanguage)
                } else {
                    alert.location
                }
                
                alert.copy(
                    title = translatedTitle,
                    content = translatedContent,
                    location = translatedLocation
                )
            } catch (e: Exception) {
                // If translation fails, return original alert
                alert
            }
        }
    }
}
