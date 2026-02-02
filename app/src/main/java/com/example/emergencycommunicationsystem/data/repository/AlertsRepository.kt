package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.data.local.AlertDao
import com.example.emergencycommunicationsystem.data.local.toDomain
import com.example.emergencycommunicationsystem.data.local.toEntity
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.Poll
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.util.NetworkUtils
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
        emit(Resource.Loading)

        val currentLanguage = UserPrefs.getLanguage(context).first()

        val cache = alertDao.getAllAlerts().map { list -> list.map { it.toDomain() } }.first()
        if (cache.isNotEmpty()) {
            val translatedCache = translateAlertsIfNeeded(cache, currentLanguage)
            emit(Resource.Success(translatedCache))
        }

        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val response = ApiClient.alertsApiService().getAlerts(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val networkAlerts = response.body()?.alerts ?: emptyList()
                    
                    alertDao.clearAlerts()
                    alertDao.insertAlerts(networkAlerts.map { it.toEntity() })
                }
            } catch (e: Exception) {
                if (cache.isEmpty()) {
                    emit(Resource.Error("Could not connect to server and no cached data found."))
                }
            }
        } else if (cache.isEmpty()) {
            emit(Resource.Error("No internet connection and no cached alerts found."))
        }

        emitAll(alertDao.getAllAlerts().map { list ->
            val alerts = list.map { it.toDomain() }
            val translatedAlerts = translateAlertsIfNeeded(alerts, currentLanguage)
            Resource.Success(translatedAlerts)
        })
    }

    suspend fun acknowledgeAlert(alertId: Int, userId: Int): Boolean {
        return try {
            val response = ApiClient.alertsApiService().acknowledgeAlert(
                mapOf("alert_id" to alertId, "user_id" to userId)
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getActivePoll(userId: Int?): Poll? {
        return try {
            val response = ApiClient.alertsApiService().getActivePoll(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.poll
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun respondToPoll(pollId: Int, userId: Int, status: String): Boolean {
        return try {
            val response = ApiClient.alertsApiService().respondToSafePoll(
                mapOf("poll_id" to pollId, "user_id" to userId, "status" to status)
            )
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun translateAlertsIfNeeded(
        alerts: List<Alert>,
        targetLanguage: String
    ): List<Alert> {
        if (targetLanguage == "en") {
            return alerts 
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
                alert
            }
        }
    }
}
