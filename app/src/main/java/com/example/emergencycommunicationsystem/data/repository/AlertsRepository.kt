package com.example.emergencycommunicationsystem.data.repository

import com.example.emergencycommunicationsystem.data.local.AlertDao
import com.example.emergencycommunicationsystem.data.local.toDomain
import com.example.emergencycommunicationsystem.data.local.toEntity
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class AlertsRepository(private val alertDao: AlertDao) {

    /**
     * Returns a Flow of alerts with a true Offline-First strategy.
     */
    fun getAlerts(userId: Int?): Flow<Resource<List<Alert>>> = flow {
        // 1. Emit Loading state
        emit(Resource.Loading)

        // 2. Fetch the current cache from Room once and emit it as Success
        // This allows the user to see data IMMEDIATELY.
        val cache = alertDao.getAllAlerts().map { list -> list.map { it.toDomain() } }.first()
        if (cache.isNotEmpty()) {
            emit(Resource.Success(cache))
        }

        try {
            // 3. Try to get fresh data from the network
            val response = ApiClient.alertsApiService().getAlerts(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val networkAlerts = response.body()?.alerts ?: emptyList()
                
                // 4. Update the local database
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

        // 5. Finally, observe the database flow for any future changes
        emitAll(alertDao.getAllAlerts().map { list ->
            Resource.Success(list.map { it.toDomain() })
        })
    }
}
