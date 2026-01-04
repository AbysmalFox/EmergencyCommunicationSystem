package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.LoginRequest
import com.example.emergencycommunicationsystem.data.network.ApiClient
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

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required.")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = ApiClient.authApiService.loginUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse == null) {
                        val errorMsg = "Login failed: Server returned an empty or invalid response."
                        Log.e("LoginViewModel", errorMsg)
                        _loginState.value = LoginState.Error(errorMsg)
                        return@launch
                    }

                    if (authResponse.success) {
                        val userId = authResponse.userId
                        val username = authResponse.username
                        val responseEmail = authResponse.email
                        val phone = authResponse.phone
                        val token = authResponse.token

                        if (userId != null && username != null && responseEmail != null && phone != null && token != null) {
                            _loginState.value = LoginState.Success(
                                authResponse.message,
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
                            Log.e("LoginViewModel", "$errorMsg. Full response: $authResponse")
                            _loginState.value = LoginState.Error(errorMsg)
                        }
                    } else {
                        val errorMsg = authResponse.message.takeIf { it.isNotBlank() } ?: "Login failed: Invalid credentials."
                        Log.w("LoginViewModel", "Login rejected by backend: $errorMsg")
                        _loginState.value = LoginState.Error(errorMsg)
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "No error body"
                    val errorMsg = "Server error (${response.code()}): $errorBodyString"
                    Log.e("LoginViewModel", "HTTP Error ${response.code()}: $errorBodyString")
                    _loginState.value = LoginState.Error(errorMsg)
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _loginState.value = LoginState.Error("HTTP Exception: ${e.code()} - ${errorBody ?: e.message}")
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
}