package com.example.emergencycommunicationsystem.ui.screens

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.LoginRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
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
}