package com.example.emergencycommunicationsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.local.AppDatabase
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.models.Poll
import com.example.emergencycommunicationsystem.data.repository.AlertsRepository
import com.example.emergencycommunicationsystem.util.LocationUtils
import com.example.emergencycommunicationsystem.util.Resource
import com.example.emergencycommunicationsystem.data.UserPrefs
import com.example.emergencycommunicationsystem.util.TranslationService
import com.example.emergencycommunicationsystem.util.LogFilter
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class AlertWithDistance(
    val alert: com.example.emergencycommunicationsystem.data.models.Alert,
    val distanceKm: Double?
)

sealed class AlertsEvent {
    data class ShowUndoSnackbar(val alertId: Int, val message: String) : AlertsEvent()
}

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlertsRepository
    private val _uiState = MutableStateFlow<Resource<List<AlertWithDistance>>>(Resource.Loading)
    val uiState: StateFlow<Resource<List<AlertWithDistance>>> = _uiState.asStateFlow()

    private val _events = Channel<AlertsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _activePoll = MutableStateFlow<Poll?>(null)
    val activePoll = _activePoll.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    
    private var loadAlertsJob: Job? = null
    
    private val _serverStatus = MutableStateFlow<String>("Unknown")
    val serverStatus: StateFlow<String> = _serverStatus.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AlertsRepository(
            database.alertDao(),
            application
        )
        loadAlerts()
        checkActivePoll()
        checkServerConnection()
    }

    fun checkServerConnection() {
        viewModelScope.launch {
            try {
                // We'll use the repository to check connection
                val userId = AuthManager.getUserId().takeIf { it > 0 }
                val response = com.example.emergencycommunicationsystem.data.network.ApiClient.alertsApiService().getAlerts(userId)
                if (response.isSuccessful) {
                    _serverStatus.value = "Online"
                } else {
                    _serverStatus.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _serverStatus.value = "Offline: ${e.message}"
            }
        }
    }

    fun loadAlerts() {
        loadAlertsJob?.cancel()
        loadAlertsJob = viewModelScope.launch {
            val userId = AuthManager.getUserId().takeIf { it > 0 }
            
            repository.getAlerts(userId).collect { resource ->
                when (resource) {
                    is com.example.emergencycommunicationsystem.util.Resource.Success<List<com.example.emergencycommunicationsystem.data.models.Alert>> -> {
                        val location = _userLocation.value
                        val alertsWithDistance = withContext(Dispatchers.Default) {
                            resource.data.map { alert ->
                                val distance = if (location != null && alert.latitude != null && alert.longitude != null) {
                                    LocationUtils.calculateDistance(location.first, location.second, alert.latitude, alert.longitude)
                                } else {
                                    null
                                }
                                AlertWithDistance(alert, distance)
                            }
                        }
                        _uiState.value = com.example.emergencycommunicationsystem.util.Resource.Success(alertsWithDistance)
                    }
                    is com.example.emergencycommunicationsystem.util.Resource.Error -> _uiState.value = com.example.emergencycommunicationsystem.util.Resource.Error(resource.message)
                    is com.example.emergencycommunicationsystem.util.Resource.Loading -> _uiState.value = com.example.emergencycommunicationsystem.util.Resource.Loading
                }
            }
        }
    }

    fun acknowledgeAlert(alertId: Int) {
        viewModelScope.launch {
            val userId = AuthManager.getUserId()
            val location = _userLocation.value
            LogFilter.d("AlertsViewModel", "Attempting to acknowledge alert $alertId for user $userId at $location")
            if (userId > 0) {
                val result = repository.acknowledgeAlert(
                    alertId = alertId,
                    userId = userId,
                    latitude = location?.first,
                    longitude = location?.second
                )
                
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        LogFilter.i("AlertsViewModel", "Successfully acknowledged alert $alertId")
                        // REMOVED Snackbar event as per user request to remove "Alert acknowledged" black bar
                        // _events.send(AlertsEvent.ShowUndoSnackbar(alertId, "Alert acknowledged"))
                        loadAlerts() // Refresh list to update UI state
                    } else {
                        val error = result.exceptionOrNull()
                        LogFilter.e("AlertsViewModel", "Failed to acknowledge alert $alertId on server", error)
                        Toast.makeText(
                            getApplication(), 
                            "Server Error: ${error?.message ?: "Unknown error"}. Updated locally only.", 
                            Toast.LENGTH_LONG
                        ).show()
                        loadAlerts() // Still refresh to show local update
                    }
                }
            } else {
                LogFilter.e("AlertsViewModel", "Invalid User ID: $userId. Cannot acknowledge.")
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Please log in to acknowledge alerts", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun undoAcknowledge(alertId: Int) {
        viewModelScope.launch {
            val userId = AuthManager.getUserId()
            if (userId > 0) {
                repository.unacknowledgeAlert(alertId, userId)
                loadAlerts()
            }
        }
    }

    private fun checkActivePoll() {
        viewModelScope.launch {
            val userId = AuthManager.getUserId().takeIf { it > 0 }
            val poll = repository.getActivePoll(userId)
            _activePoll.value = poll
        }
    }

    fun respondToPoll(pollId: Int, status: String) {
        viewModelScope.launch {
            val userId = AuthManager.getUserId()
            val location = _userLocation.value
            if (userId > 0) {
                val result = repository.respondToPoll(
                    pollId = pollId,
                    userId = userId,
                    status = status,
                    latitude = location?.first,
                    longitude = location?.second
                )
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _activePoll.value = null // Close the dialog
                        LogFilter.i("AlertsViewModel", "Successfully responded to poll $pollId")
                        Toast.makeText(getApplication(), "Response sent successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        val error = result.exceptionOrNull()
                        LogFilter.e("AlertsViewModel", "Failed to respond to poll $pollId", error)
                        Toast.makeText(
                            getApplication(), 
                            "Failed to send response: ${error?.message ?: "Unknown error"}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Please log in to respond", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun dismissPoll() {
        _activePoll.value = null
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is com.example.emergencycommunicationsystem.util.Resource.Success<List<AlertWithDistance>>) {
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
                _uiState.value = com.example.emergencycommunicationsystem.util.Resource.Success(alertsWithDistance)
            }
        }
    }
}