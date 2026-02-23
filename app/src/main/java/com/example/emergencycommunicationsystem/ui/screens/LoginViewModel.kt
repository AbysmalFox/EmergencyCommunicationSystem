package com.example.emergencycommunicationsystem.ui.screens

import android.app.Application
import android.content.Intent
import android.util.Base64
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
import org.json.JSONObject
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
                val errorMsg = try {
                    if (errorBody != null) {
                        JSONObject(errorBody).optString("message", e.response()?.message() ?: "Bad Request")
                    } else {
                        e.response()?.message() ?: "Bad Request"
                    }
                } catch (_: Exception) {
                    e.response()?.message() ?: e.message()
                }
                _loginState.value = LoginState.Error("Error: $errorMsg")
                Log.e("LoginViewModel", "HttpException: ${e.code()} - $errorBody", e)

            } catch (e: IOException) {
                _loginState.value = LoginState.Error("Network error: Could not connect to server.")
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
     */
    fun handleGoogleSignInResult(data: Intent?) {
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            try {
                val account = GoogleSignInHelper.handleSignInResult(data)
                
                if (account != null) {
                    loginWithGoogleAccount(account)
                } else {
                    Log.e("LoginViewModel", "Google Sign-In account is null")
                    _loginState.value = LoginState.Error("Google Sign-In failed.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Exception in handleGoogleSignInResult: ${e.message}", e)
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Error processing Google Sign-In")
            }
        }
    }
    
    /**
     * Login using Google account information
     */
    private suspend fun loginWithGoogleAccount(account: GoogleSignInAccount) {
        try {
            val idToken = account.idToken
            val email = account.email ?: ""
            val displayName = account.displayName ?: ""
            
            if (idToken == null) {
                Log.e("LoginViewModel", "Google ID Token is null for: $email")
                _loginState.value = LoginState.Error("Google Sign-In: ID token is missing")
                return
            }

            logGoogleTokenClaims(idToken)
            
            val loginData = mutableMapOf<String, Any>()
            loginData["email"] = email
            loginData["google_token"] = idToken
            loginData["google_id"] = account.id ?: ""
            loginData["name"] = displayName
            loginData["device_id"] = com.example.emergencycommunicationsystem.util.DeviceManager.getDeviceId(getApplication())
            loginData["device_type"] = "android"
            loginData["device_name"] = com.example.emergencycommunicationsystem.util.DeviceManager.getDeviceName()
            loginData["push_token"] = com.example.emergencycommunicationsystem.util.DeviceManager.getPushToken()
            
            Log.d("LoginViewModel", "====================================================")
            Log.d("LoginViewModel", "🚀 SENDING GOOGLE LOGIN TO BACKEND")
            Log.d("LoginViewModel", "Email: $email")
            Log.d("LoginViewModel", "Name: $displayName")
            Log.d("LoginViewModel", "Device ID: ${loginData["device_id"]}")
            Log.d("LoginViewModel", "====================================================")
            
            val response = authRepository.loginWithGoogle(getApplication(), loginData)
            
            if (response.success) {
                Log.i("LoginViewModel", "Google Login Success: $email")
                val userId = response.userId
                val username = response.username ?: displayName
                val responseEmail = response.email ?: email
                val phone = response.phone ?: ""
                val token = response.token
                
                if (userId != null && token != null) {
                    _loginState.value = LoginState.Success(
                        response.message, userId, username, responseEmail, phone, token
                    )
                } else {
                    _loginState.value = LoginState.Error("Google Login succeeded, but user data is incomplete.")
                }
            } else {
                Log.w("LoginViewModel", "Google Login Rejected: ${response.message}")
                _loginState.value = LoginState.Error(response.message.takeIf { it.isNotBlank() } ?: "Google Sign-In failed.")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorCode = e.code()
            
            val backendMsg = try {
                if (errorBody != null) {
                    val json = JSONObject(errorBody)
                    json.optString("message", json.optString("error", "Unknown error"))
                } else null
            } catch (_: Exception) {
                null
            }

            Log.e("LoginViewModel", "====================================================")
            Log.e("LoginViewModel", "❌ BACKEND HTTP ERROR ($errorCode)")
            Log.e("LoginViewModel", "Body: $errorBody")
            Log.e("LoginViewModel", "URL: ${e.response()?.raw()?.request?.url}")
            Log.e("LoginViewModel", "====================================================")
            
            if (errorCode == 400 && backendMsg?.contains("password", ignoreCase = true) == true) {
                Log.e("LoginViewModel", "CRITICAL: Backend is demanding a password for a Google Login. Fix the backend login.php.")
                _loginState.value = LoginState.Error("Server error: Google Auth requires backend fix.")
            } else {
                _loginState.value = LoginState.Error(backendMsg ?: "Server Error $errorCode")
            }
        } catch (e: IOException) {
            Log.e("LoginViewModel", "Network Error during Google login", e)
            _loginState.value = LoginState.Error("Network error: Could not connect to server.")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Unexpected error during Google login", e)
            _loginState.value = LoginState.Error("An unexpected error occurred.")
        }
    }

    private fun logGoogleTokenClaims(idToken: String) {
        try {
            val parts = idToken.split(".")
            if (parts.size < 2) {
                Log.w("LoginViewModel", "Google token format invalid (not JWT)")
                return
            }

            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = String(payloadBytes, Charsets.UTF_8)
            val json = JSONObject(payload)

            val aud = json.optString("aud", "")
            val azp = json.optString("azp", "")
            val iss = json.optString("iss", "")
            val sub = json.optString("sub", "")

            Log.i("LoginViewModel", "Google token claims: aud=$aud azp=$azp iss=$iss sub=${sub.take(8)}...")
        } catch (e: Exception) {
            Log.w("LoginViewModel", "Failed to decode Google token claims", e)
        }
    }
}
