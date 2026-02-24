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
import com.example.emergencycommunicationsystem.util.LogFilter
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
                LogFilter.d("AlertsRepository", "Fetching alerts from server for userId: $userId")
                val response = ApiClient.alertsApiService().getAlerts(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val networkAlerts = response.body()?.alerts ?: emptyList()
                    LogFilter.d("AlertsRepository", "Successfully fetched ${networkAlerts.size} alerts from server")
                    
                    // Get current cached alerts to preserve local-only state if necessary
                    val currentCache = alertDao.getAllAlerts().first()
                    val acknowledgedIds = currentCache.filter { it.isAcknowledged }.map { it.id }.toSet()
                    
                    val entitiesToInsert = networkAlerts.map { alert ->
                        val entity = alert.toEntity()
                        // If locally acknowledged but server says false, keep it true for now (optimistic)
                        if (alert.id in acknowledgedIds && !alert.isAcknowledged) {
                            entity.copy(isAcknowledged = true)
                        } else {
                            entity
                        }
                    }
                    
                    alertDao.insertAlerts(entitiesToInsert)
                    UserPrefs.saveAlertsLastSyncMillis(context, System.currentTimeMillis())
                } else {
                    val errorBody = response.errorBody()?.string()
                    LogFilter.e("AlertsRepository", "Failed to fetch alerts. Code: ${response.code()}, Error: $errorBody")
                }
            } catch (e: Exception) {
                LogFilter.e("AlertsRepository", "Network exception fetching alerts", e)
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

    suspend fun acknowledgeAlert(alertId: Int, userId: Int, latitude: Double? = null, longitude: Double? = null): Result<Unit> {
        // Optimistically update local DB first for instant UI response
        try {
            alertDao.updateAcknowledgeStatus(alertId)
            LogFilter.d("AlertsRepository", "Locally acknowledged alert $alertId")
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Failed to update local status for alert $alertId", e)
        }

        return try {
            LogFilter.d("AlertsRepository", "Sending acknowledgement to server: alertId=$alertId, userId=$userId, lat=$latitude, lon=$longitude")
            
            val request = com.example.emergencycommunicationsystem.network.AcknowledgeRequest(
                alertId = alertId,
                userId = userId,
                status = "received",
                latitude = latitude,
                longitude = longitude
            )

            val response = ApiClient.alertsApiService().acknowledgeAlert(request)
            
            if (response.isSuccessful) {
                LogFilter.d("AlertsRepository", "Server successfully acknowledged alert $alertId. Response: ${response.body()}")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Server error ${response.code()}: $errorBody"
                LogFilter.e("AlertsRepository", "Failed to acknowledge alert $alertId on server: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Network exception during acknowledgement for alert $alertId", e)
            Result.failure(e)
        }
    }

    suspend fun unacknowledgeAlert(alertId: Int, userId: Int): Result<Unit> {
        try {
            alertDao.revertAcknowledgeStatus(alertId)
            LogFilter.d("AlertsRepository", "Locally un-acknowledged alert $alertId")
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Failed to revert local status for alert $alertId", e)
        }

        return try {
            LogFilter.d("AlertsRepository", "Sending un-acknowledgement to server: alertId=$alertId, userId=$userId")
            val response = ApiClient.alertsApiService().unacknowledgeAlert(
                mapOf("alert_id" to alertId, "user_id" to userId)
            )
            
            if (response.isSuccessful) {
                LogFilter.d("AlertsRepository", "Server successfully un-acknowledged alert $alertId")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                LogFilter.e("AlertsRepository", "Failed to un-acknowledge on server: $errorBody")
                Result.success(Unit) // Still return success because local state was reverted
            }
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Network exception during un-acknowledgement", e)
            Result.success(Unit)
        }
    }

    suspend fun getActivePoll(userId: Int?): Poll? {
        return try {
            LogFilter.d("AlertsRepository", "Fetching active poll for userId: $userId")
            val response = ApiClient.alertsApiService().getActivePoll(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val poll = response.body()?.poll
                LogFilter.d("AlertsRepository", "Active poll found: ${poll?.id}")
                poll
            } else {
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    LogFilter.e("AlertsRepository", "Failed to fetch active poll. Code: ${response.code()}, Error: $errorBody")
                }
                null
            }
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Network exception fetching active poll", e)
            null
        }
    }

    suspend fun respondToPoll(pollId: Int, userId: Int, status: String, latitude: Double? = null, longitude: Double? = null): Result<Unit> {
        return try {
            LogFilter.d("AlertsRepository", "Sending poll response: pollId=$pollId, userId=$userId, status=$status, lat=$latitude, lon=$longitude")
            
            val request = com.example.emergencycommunicationsystem.network.PollResponseRequest(
                pollId = pollId,
                userId = userId,
                status = status,
                latitude = latitude,
                longitude = longitude
            )

            val response = ApiClient.alertsApiService().respondToSafePoll(request)
            
            if (response.isSuccessful) {
                LogFilter.d("AlertsRepository", "Successfully responded to poll $pollId. Response: ${response.body()}")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = "Server error ${response.code()}: $errorBody"
                LogFilter.e("AlertsRepository", "Failed to respond to poll $pollId on server: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            LogFilter.e("AlertsRepository", "Network exception during poll response for poll $pollId", e)
            Result.failure(e)
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

                val translatedMessage = if (!alert.message.isNullOrBlank()) {
                    TranslationService.translate(alert.message, targetLanguage)
                } else {
                    alert.message
                }
                
                val translatedLocation = if (!alert.location.isNullOrBlank()) {
                    TranslationService.translate(alert.location, targetLanguage)
                } else {
                    alert.location
                }
                
                alert.copy(
                    title = translatedTitle,
                    message = translatedMessage,
                    content = translatedContent,
                    location = translatedLocation
                )
            } catch (e: Exception) {
                alert
            }
        }
    }
}
