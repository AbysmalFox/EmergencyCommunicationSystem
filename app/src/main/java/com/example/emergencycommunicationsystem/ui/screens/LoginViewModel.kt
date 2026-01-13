package com.example.emergencycommunicationsystem.ui.screens

import android.app.Application
import android.content.Intent
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.LoginRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import com.example.emergencycommunicationsystem.util.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Define the possible states for the login UI
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String, val userId: Int, val username: String, val email: String, val phone: String, val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(emailOrPhone: String, password: String) {
        if (emailOrPhone.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email/Phone and password are required.")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val isEmail = Patterns.EMAIL_ADDRESS.matcher(emailOrPhone).matches()
                val request = if (isEmail) {
                    LoginRequest(email = emailOrPhone, password = password)
                } else {
                    LoginRequest(phone = emailOrPhone, password = password)
                }

                val response = authRepository.login(getApplication(), request)

                if (response.success) {
                    val userId = response.userId
                    val username = response.username
                    val responseEmail = response.email
                    val phone = response.phone
                    val token = response.token

                    if (userId != null && username != null && responseEmail != null && phone != null && token != null) {
                        _loginState.value = LoginState.Success(
                            response.message,
                            userId,
                            username,
                            responseEmail,
                            phone,
                            token
                        )
                    } else {
                        val missingFields = mutableListOf<String>()
                        if (userId == null) missingFields.add("userId")
                        if (username == null) missingFields.add("username")
                        if (responseEmail == null) missingFields.add("email")
                        if (phone == null) missingFields.add("phone")
                        if (token == null) missingFields.add("token")
                        val errorMsg = "Login succeeded, but the server response was incomplete. Missing fields: ${missingFields.joinToString()}"
                        Log.e("LoginViewModel", "$errorMsg. Full response: $response")
                        _loginState.value = LoginState.Error(errorMsg)
                    }
                } else {
                    val errorMsg = response.message.takeIf { it.isNotBlank() } ?: "Login failed: Invalid credentials."
                    Log.w("LoginViewModel", "Login rejected by backend: $errorMsg")
                    _loginState.value = LoginState.Error(errorMsg)
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = e.response()?.let { res ->
                    try {
                        // Assuming you have a standard error response model
                        // val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        // errorResponse.message ?: res.message()
                         res.message()
                    } catch (jsonE: Exception) {
                        res.message()
                    }
                } ?: e.message()
                _loginState.value = LoginState.Error("HTTP Error ${e.code()}: $errorMsg")
                Log.e("LoginViewModel", "HttpException: ${e.code()} - $errorBody", e)

            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Network error: Could not connect to server. Check your connection.")
                Log.e("LoginViewModel", "IOException during login: ${e.message}", e)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("An unexpected error occurred: ${e.localizedMessage}")
                Log.e("LoginViewModel", "Unexpected error during login", e)
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    /**
     * Handle Google Sign-In result
     * This will send the Google account info to the backend for authentication
     */
    fun handleGoogleSignInResult(data: Intent?) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            try {
                val account = GoogleSignInHelper.handleSignInResult(data)
                
                if (account != null) {
                    // Use Google account to login/register with backend
                    loginWithGoogleAccount(account)
                } else {
                    _loginState.value = LoginState.Error("Google Sign-In failed. Please try again.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error processing Google Sign-In: ${e.localizedMessage}")
                Log.e("LoginViewModel", "Error handling Google Sign-In", e)
            }
        }
    }
    
    /**
     * Login using Google account information
     * This sends the Google ID token to the backend
     */
    private suspend fun loginWithGoogleAccount(account: GoogleSignInAccount) {
        try {
            val idToken = account.idToken
            val email = account.email ?: ""
            val displayName = account.displayName ?: ""
            
            if (idToken == null) {
                _loginState.value = LoginState.Error("Google Sign-In: ID token is missing")
                return
            }
            
            // Create login request with Google token
            // Note: You may need to adjust this based on your backend API
            // Some backends expect a special "google_token" field or similar
            val loginData = mutableMapOf<String, Any>()
            loginData["email"] = email
            loginData["google_token"] = idToken
            loginData["google_id"] = account.id ?: ""
            loginData["name"] = displayName
            
            loginData["device_id"] = com.example.emergencycommunicationsystem.util.DeviceManager.getDeviceId(getApplication())
            loginData["device_type"] = "android"
            loginData["device_name"] = com.example.emergencycommunicationsystem.util.DeviceManager.getDeviceName()
            loginData["push_token"] = com.example.emergencycommunicationsystem.util.DeviceManager.getPushToken()
            
            // Call your backend API for Google login
            // You may need to create a separate endpoint or modify the existing one
            val response = authRepository.loginWithGoogle(getApplication(), loginData)
            
            if (response.success) {
                val userId = response.userId
                val username = response.username ?: displayName
                val responseEmail = response.email ?: email
                val phone = response.phone ?: ""
                val token = response.token
                
                if (userId != null && username != null && responseEmail.isNotEmpty() && token != null) {
                    _loginState.value = LoginState.Success(
                        response.message,
                        userId,
                        username,
                        responseEmail,
                        phone,
                        token
                    )
                } else {
                    _loginState.value = LoginState.Error("Login succeeded, but the server response was incomplete.")
                }
            } else {
                val errorMsg = response.message.takeIf { it.isNotBlank() } ?: "Google Sign-In failed."
                _loginState.value = LoginState.Error(errorMsg)
            }
        } catch (e: HttpException) {
            val errorMsg = e.response()?.message() ?: e.message()
            _loginState.value = LoginState.Error("HTTP Error ${e.code()}: $errorMsg")
            Log.e("LoginViewModel", "HttpException during Google login: ${e.code()}", e)
        } catch (e: IOException) {
            _loginState.value = LoginState.Error("Network error: Could not connect to server.")
            Log.e("LoginViewModel", "IOException during Google login", e)
        } catch (e: Exception) {
            _loginState.value = LoginState.Error("An unexpected error occurred: ${e.localizedMessage}")
            Log.e("LoginViewModel", "Unexpected error during Google login", e)
        }
    }
}