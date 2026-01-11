package com.example.emergencycommunicationsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.local.AppDatabase
import com.example.emergencycommunicationsystem.data.models.Alert
import com.example.emergencycommunicationsystem.data.repository.AlertsRepository
import com.example.emergencycommunicationsystem.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlertsRepository
    private val _uiState = MutableStateFlow<Resource<List<Alert>>>(Resource.Loading)
    val uiState: StateFlow<Resource<List<Alert>>> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AlertsRepository(database.alertDao())
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            val userId = AuthManager.getUserId().takeIf { it > 0 }
            repository.getAlerts(userId).collect { resource ->
                _uiState.value = resource
            }
        }
    }
}
