package com.example.emergencycommunicationsystem

import android.content.Context
import android.content.SharedPreferences
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone" // Added phone key
    private const val KEY_PROFILE_PIC = "profile_pic"
    private const val KEY_TOKEN = "auth_token"

    private lateinit var sharedPrefs: SharedPreferences
    private val authRepository = AuthRepository()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> get() = _isLoggedIn
    
    // Flow to notify subscribers of user data changes
    val userDataFlow = MutableSharedFlow<Unit>(replay = 1)

    fun initialize(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isLoggedIn.value = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        userDataFlow.tryEmit(Unit)
    }

    fun saveLoginState(userId: Int, username: String, email: String, phone: String, token: String, profilePic: String? = null) {
        sharedPrefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone) // Save phone number
            if (profilePic != null) putString(KEY_PROFILE_PIC, profilePic)
            putString(KEY_TOKEN, token)
            apply()
        }
        _isLoggedIn.value = true
        userDataFlow.tryEmit(Unit)
    }

    fun saveProfilePic(uri: String) {
        sharedPrefs.edit().putString(KEY_PROFILE_PIC, uri).apply()
        userDataFlow.tryEmit(Unit)
    }

    suspend fun logout(context: Context) {
        val userId = getUserId()
        if (userId != -1) {
            try {
                authRepository.logout(context, userId)
            } catch (e: Exception) {
                // Log the exception, but proceed with local logout
            }
        }
        sharedPrefs.edit().clear().apply()
        _isLoggedIn.value = false
    }

    fun getUserId(): Int = sharedPrefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String? = sharedPrefs.getString(KEY_USERNAME, null)
    fun getEmail(): String? = sharedPrefs.getString(KEY_EMAIL, null)
    fun getPhone(): String? = sharedPrefs.getString(KEY_PHONE, null) // Retrieve phone number
    fun getProfilePic(): String? = sharedPrefs.getString(KEY_PROFILE_PIC, null)
    fun getToken(): String? = sharedPrefs.getString(KEY_TOKEN, null)
}