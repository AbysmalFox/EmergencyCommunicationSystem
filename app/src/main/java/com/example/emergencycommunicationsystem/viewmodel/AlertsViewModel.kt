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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            repository.getAlerts(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val location = _userLocation.value
                        val alertsWithDistance = resource.data.map { alert ->
                            val distance = if (location != null && alert.latitude != null && alert.longitude != null) {
                                LocationUtils.calculateDistance(location.first, location.second, alert.latitude, alert.longitude)
                            } else {
                                null
                            }
                            AlertWithDistance(alert, distance)
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
        if (_uiState.value is Resource.Success) {
            val alerts = (_uiState.value as Resource.Success<List<AlertWithDistance>>).data.map { it.alert }
            val alertsWithDistance = alerts.map { alert ->
                val distance = if (alert.latitude != null && alert.longitude != null) {
                    LocationUtils.calculateDistance(latitude, longitude, alert.latitude, alert.longitude)
                } else {
                    null
                }
                AlertWithDistance(alert, distance)
            }
            _uiState.value = Resource.Success(alertsWithDistance)
        }
    }
}
