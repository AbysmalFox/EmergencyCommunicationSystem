package com.example.emergencycommunicationsystem.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val message: String) : ReportState()
    data class Error(val message: String) : ReportState()
}

class ReportIncidentViewModel : ViewModel() {

    private val incidentRepository = IncidentRepository()

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState

    fun submitReport(
        context: Context,
        userId: Int,
        incidentType: String,
        urgency: String,
        details: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        reporterName: String?,
        imageUri: Uri?
    ) {
        _reportState.value = ReportState.Loading
        viewModelScope.launch {
            try {
                val response = incidentRepository.submitIncident(
                    context,
                    userId,
                    incidentType,
                    urgency,
                    details,
                    latitude,
                    longitude,
                    address,
                    reporterName,
                    imageUri
                )
                if (response.success) {
                    _reportState.value = ReportState.Success(response.message)
                } else {
                    _reportState.value = ReportState.Error(response.message)
                }
            } catch (e: Exception) {
                _reportState.value = ReportState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        _reportState.value = ReportState.Idle
    }
}