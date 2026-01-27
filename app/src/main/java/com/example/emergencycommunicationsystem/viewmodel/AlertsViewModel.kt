package com.example.emergencycommunicationsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.local.AppDatabase
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.repository.AlertsRepository
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.util.TranslationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AlertWithDistance(
    val alert: Alert,
    val distanceKm: Double?
)

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlertsRepository
    private val _uiState = MutableStateFlow<Resource<List<AlertWithDistance>>>(Resource.Loading)
    val uiState: StateFlow<Resource<List<AlertWithDistance>>> = _uiState.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AlertsRepository(
            database.alertDao(),
            application
        )
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            val userId = AuthManager.getUserId().takeIf { it > 0 }
            val context = getApplication<Application>().applicationContext
            
            combine(
                repository.getAlerts(userId),
                UserPrefs.getLanguage(context)
            ) { resource, language ->
                Pair(resource, language)
            }.collect { (resource, language) ->
                when (resource) {
                    is Resource.Success -> {
                        val location = _userLocation.value
                        val alertsWithDistance = withContext(Dispatchers.Default) {
                            resource.data.map { alert ->
                                // Translate Alert Content
                                val translatedTitle = TranslationService.translate(alert.title ?: "", language)
                                val translatedContent = TranslationService.translate(alert.content ?: "", language)
                                val translatedAlert = alert.copy(title = translatedTitle, content = translatedContent)
                                
                                val distance = if (location != null && translatedAlert.latitude != null && translatedAlert.longitude != null) {
                                    LocationUtils.calculateDistance(location.first, location.second, translatedAlert.latitude, translatedAlert.longitude)
                                } else {
                                    null
                                }
                                AlertWithDistance(translatedAlert, distance)
                            }
                        }
                        _uiState.value = Resource.Success(alertsWithDistance)
                    }
                    is Resource.Error -> _uiState.value = Resource.Error(resource.message)
                    is Resource.Loading -> _uiState.value = Resource.Loading
                }
            }
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        // Re-calculate distances when location changes
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is Resource.Success) {
                val alerts = currentState.data.map { it.alert }
                val alertsWithDistance = withContext(Dispatchers.Default) {
                    alerts.map { alert ->
                        val distance = if (alert.latitude != null && alert.longitude != null) {
                            LocationUtils.calculateDistance(latitude, longitude, alert.latitude, alert.longitude)
                        } else {
                            null
                        }
                        AlertWithDistance(alert, distance)
                    }
                }
                _uiState.value = Resource.Success(alertsWithDistance)
            }
        }
    }
}
