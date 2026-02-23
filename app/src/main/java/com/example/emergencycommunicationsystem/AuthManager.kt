package com.example.emergencycommunicationsystem

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object AuthManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone" // Added phone key
    private const val KEY_PROFILE_PIC = "profile_pic"
    private const val KEY_TOKEN = "auth_token"
    private const val TAG = "AuthManager"

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var appContext: Context
    private val authRepository = AuthRepository()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> get() = _isLoggedIn
    
    private val _username = MutableStateFlow<String?>(null)
    val usernameFlow: StateFlow<String?> = _username

    private val _email = MutableStateFlow<String?>(null)
    val emailFlow: StateFlow<String?> = _email

    private val _phone = MutableStateFlow<String?>(null)
    val phoneFlow: StateFlow<String?> = _phone

    private val _profilePic = MutableStateFlow<String?>(null)
    val profilePicFlow: StateFlow<String?> = _profilePic

    // Flow to notify subscribers of user data changes
    val userDataFlow = MutableSharedFlow<Unit>(replay = 1)

    fun initialize(context: Context) {
        appContext = context.applicationContext
        sharedPrefs = createSecurePrefs(context)
        val loggedIn = sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        _isLoggedIn.value = loggedIn
        if (loggedIn) {
            _username.value = sharedPrefs.getString(KEY_USERNAME, null)
            _email.value = sharedPrefs.getString(KEY_EMAIL, null)
            _phone.value = sharedPrefs.getString(KEY_PHONE, null)
            _profilePic.value = sharedPrefs.getString(KEY_PROFILE_PIC, null)
        }
        userDataFlow.tryEmit(Unit)
    }

    private fun createSecurePrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences init failed; using non-sensitive fallback prefs", e)
            context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
        }
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
        _username.value = username
        _email.value = email
        _phone.value = phone
        if (profilePic != null) _profilePic.value = profilePic
        
        _isLoggedIn.value = true
        userDataFlow.tryEmit(Unit)

        // Fetch and upload FCM token on login
        uploadFcmToken(userId)
    }

    private fun uploadFcmToken(userId: Int) {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                val deviceId = android.provider.Settings.Secure.getString(appContext.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val apiService = com.example.emergencycommunicationsystem.data.network.ApiClient.settingsApiService()
                        apiService.updateFcmToken(com.example.emergencycommunicationsystem.network.FcmTokenRequest(userId, deviceId, fcmToken))
                    } catch (e: Exception) {
                        android.util.Log.e("AuthManager", "Failed to upload FCM token", e)
                    }
                }
            }
        }
    }

    fun saveProfilePic(uri: String) {
        sharedPrefs.edit().putString(KEY_PROFILE_PIC, uri).apply()
        _profilePic.value = uri
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
        _username.value = null
        _email.value = null
        _phone.value = null
        _profilePic.value = null
        _isLoggedIn.value = false
    }

    fun getUserId(): Int = sharedPrefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String? = sharedPrefs.getString(KEY_USERNAME, null)
    fun getEmail(): String? = sharedPrefs.getString(KEY_EMAIL, null)
    fun getPhone(): String? = sharedPrefs.getString(KEY_PHONE, null) // Retrieve phone number
    fun getProfilePic(): String? = sharedPrefs.getString(KEY_PROFILE_PIC, null)
    fun getToken(): String? = sharedPrefs.getString(KEY_TOKEN, null)
}
