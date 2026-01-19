package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.data.UpdateProfileRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: Int,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<List<SubscriptionCategory>?>(null)
    val uiState = _uiState.asStateFlow()

    private val _updateProfileResult = MutableStateFlow<Result<String>?>(null)
    val updateProfileResult = _updateProfileResult.asStateFlow()

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
                // Handle error
            }
        }
    }

    fun onSubscriptionChange(categoryId: Int, isEnabled: Boolean) {
        val currentSettings = _uiState.value?.toMutableList() ?: return
        val index = currentSettings.indexOfFirst { it.categoryId == categoryId }
        if (index != -1) {
            currentSettings[index] = currentSettings[index].copy(isSubscribed = if (isEnabled) 1 else 0)
            _uiState.value = currentSettings
        }

        viewModelScope.launch {
            try {
                settingsRepository.updateSubscription(userId, categoryId, isEnabled)
            } catch (e: Exception) {
                loadSubscriptionSettings()
            }
        }
    }

    fun updateProfile(name: String, email: String, phone: String) {
        viewModelScope.launch {
            try {
                val request = UpdateProfileRequest(userId, name, email, phone)
                val response = authRepository.updateProfile(request)
                if (response.success) {
                    // Update AuthManager with new info
                    AuthManager.saveLoginState(
                        userId = userId,
                        username = name,
                        email = email,
                        phone = phone,
                        token = AuthManager.getToken() ?: ""
                    )
                    _updateProfileResult.value = Result.success(response.message ?: "Profile updated successfully")
                } else {
                    _updateProfileResult.value = Result.failure(Exception(response.message ?: "Failed to update profile"))
                }
            } catch (e: Exception) {
                _updateProfileResult.value = Result.failure(e)
            }
        }
    }

    fun clearUpdateProfileResult() {
        _updateProfileResult.value = null
    }
}
