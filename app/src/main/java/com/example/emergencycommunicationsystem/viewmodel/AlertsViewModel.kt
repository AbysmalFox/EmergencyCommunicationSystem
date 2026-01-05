package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class AlertsUiState {
    object Loading : AlertsUiState()
    data class Success(val alerts: List<Alert>) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}

class AlertsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        _uiState.value = AlertsUiState.Loading

        viewModelScope.launch {
            try {
                val userId = AuthManager.getUserId().takeIf { it > 0 } // Get user ID, but only if they're logged in
                val response = ApiClient.alertsApiService.getAlerts(userId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        _uiState.value = AlertsUiState.Error(
                            "Failed to load alerts: empty server response."
                        )
                        return@launch
                    }

                    if (body.success) {
                        _uiState.value = AlertsUiState.Success(body.alerts)
                    } else {
                        _uiState.value = AlertsUiState.Error(
                            body.message.ifBlank { "Failed to load alerts from server." }
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    _uiState.value = AlertsUiState.Error(
                        "Server error (${response.code()}): $errorBody"
                    )
                }
            } catch (e: HttpException) {
                _uiState.value = AlertsUiState.Error(
                    "HTTP error ${e.code()}: ${e.message()}"
                )
            } catch (e: IOException) {
                _uiState.value = AlertsUiState.Error(
                    "Network error: Could not connect to the server. Please check your connection."
                )
            } catch (e: Exception) {
                _uiState.value = AlertsUiState.Error(
                    "Unexpected error: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }
}


