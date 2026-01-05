package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: Int,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<SubscriptionCategory>?>(null)
    val uiState = _uiState.asStateFlow()

    init {
        if (userId > 0) {
            loadSubscriptionSettings()
        }
    }

    private fun loadSubscriptionSettings() {
        viewModelScope.launch {
            try {
                val settings = settingsRepository.getSubscriptionSettings(userId)
                _uiState.value = settings
            } catch (e: Exception) {
                // Handle error, maybe show a toast or log the error
            }
        }
    }

    fun onSubscriptionChange(categoryId: Int, isEnabled: Boolean) {
        // Optimistically update the UI
        val currentSettings = _uiState.value?.toMutableList() ?: return
        val index = currentSettings.indexOfFirst { it.categoryId == categoryId }
        if (index != -1) {
            currentSettings[index] = currentSettings[index].copy(isSubscribed = if (isEnabled) 1 else 0)
            _uiState.value = currentSettings
        }

        // Update the backend
        viewModelScope.launch {
            try {
                settingsRepository.updateSubscription(userId, categoryId, isEnabled)
            } catch (e: Exception) {
                // If backend fails, revert the UI change and show an error
                loadSubscriptionSettings() // Reload from server to get the true state
                // Optionally, show a toast to the user
            }
        }
    }
}
