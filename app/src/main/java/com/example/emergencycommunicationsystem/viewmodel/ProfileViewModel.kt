package com.example.emergencycommunicationsystem.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.AuthManager
import com.example.emergencycommunicationsystem.data.ChangePasswordRequest
import com.example.emergencycommunicationsystem.data.SubscriptionCategory
import com.example.emergencycommunicationsystem.data.UpdateProfileRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import com.example.emergencycommunicationsystem.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val userId: Int,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow<List<SubscriptionCategory>?>(null)
    val uiState = _uiState.asStateFlow()

    private val _updateProfileResult = MutableStateFlow<Result<String>?>(null)
    val updateProfileResult = _updateProfileResult.asStateFlow()
    
    private val _changePasswordResult = MutableStateFlow<Result<String>?>(null)
    val changePasswordResult = _changePasswordResult.asStateFlow()

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
                // Fallback to default categories if API fails (for offline/demo support)
                _uiState.value = listOf(
                    SubscriptionCategory(categoryId = 1, name = "Weather", isSubscribed = 1),
                    SubscriptionCategory(categoryId = 2, name = "Earthquake", isSubscribed = 1),
                    SubscriptionCategory(categoryId = 3, name = "Fire", isSubscribed = 1),
                    SubscriptionCategory(categoryId = 4, name = "Flood", isSubscribed = 1),
                    SubscriptionCategory(categoryId = 5, name = "Traffic", isSubscribed = 0)
                )
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

    fun updateProfile(context: Context, name: String, email: String, phone: String, profilePicUri: String? = null) {
        viewModelScope.launch {
            try {
                val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
                val phoneBody = phone.toRequestBody("text/plain".toMediaTypeOrNull())
                
                var profilePicPart: MultipartBody.Part? = null
                if (profilePicUri != null) {
                    val uri = Uri.parse(profilePicUri)
                    // Create a temp file from the URI
                    val contentResolver = context.contentResolver
                    val tempFile = File(context.cacheDir, "temp_profile_pic.jpg")
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    profilePicPart = MultipartBody.Part.createFormData("profile_pic", tempFile.name, requestFile)
                    
                    // Also save locally immediately
                    AuthManager.saveProfilePic(profilePicUri)
                }

                val response = authRepository.updateProfile(userIdBody, nameBody, emailBody, phoneBody, profilePicPart)
                
                if (response.success) {
                    // Extract permanent URL from backend if available
                    val permanentPicUrl = (response.data as? Map<*, *>)?.get("profile_pic_url") as? String
                    
                    // Update AuthManager with new info
                    AuthManager.saveLoginState(
                        userId = userId,
                        username = name,
                        email = email,
                        phone = phone,
                        token = AuthManager.getToken() ?: "",
                        profilePic = permanentPicUrl ?: profilePicUri ?: AuthManager.getProfilePic()
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
    
    fun changePassword(current: String, new: String) {
        if (new.length < 8) {
            _changePasswordResult.value = Result.failure(Exception("New password must be at least 8 characters long."))
            return
        }

        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(userId, current, new)
                val response = authRepository.changePassword(request)
                if (response.success) {
                    _changePasswordResult.value = Result.success(response.message)
                } else {
                    _changePasswordResult.value = Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                _changePasswordResult.value = Result.failure(e)
            }
        }
    }

    fun clearUpdateProfileResult() {
        _updateProfileResult.value = null
    }
    
    fun clearChangePasswordResult() {
        _changePasswordResult.value = null
    }
}