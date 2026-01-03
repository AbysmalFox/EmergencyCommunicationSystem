package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository
import com.example.emergencycommunicationsystem.network.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: Int,
    private val settingsRepository: SettingsRepository = SettingsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserPreferences?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        if (userId > 0) {
            loadUserPreferences()
        }
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            try {
                val prefs = settingsRepository.getUserPreferences(userId)
                _uiState.value = prefs
            } catch (e: Exception) {
                // Handle error, maybe show a toast
            }
        }
    }

    fun onPreferenceChange(newPrefs: UserPreferences) {
        _uiState.value = newPrefs
        viewModelScope.launch {
            try {
                settingsRepository.updateUserPreferences(newPrefs)
            } catch (e: Exception) {
                // Handle error, maybe revert the UI state or show a toast
            }
        }
    }
}