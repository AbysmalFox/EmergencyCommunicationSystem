package com.example.emergencycommunicationsystem.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.RegisterRequest
import com.example.emergencycommunicationsystem.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import android.util.Log // Added import for Log

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val message: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(username: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _signUpState.value = SignUpState.Error("Passwords do not match.")
            return
        }
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _signUpState.value = SignUpState.Error("All fields are required.")
            return
        }
        if (password.length < 6) {
            _signUpState.value = SignUpState.Error("Password must be at least 6 characters long.")
            return
        }

        _signUpState.value = SignUpState.Loading

        viewModelScope.launch {
            try {
                val request = RegisterRequest(username, email, password)
                val response = RetrofitClient.authApiService.registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _signUpState.value = SignUpState.Success(authResponse.message)
                    } else {
                        // Backend returned success: false
                        // Try to get a specific message if backend sends one
                        _signUpState.value = SignUpState.Error(authResponse?.message ?: "Registration failed: Unknown reason.")
                    }
                } else {
                    // HTTP error (4xx, 5xx) - Try to parse error body for more details
                    val errorBodyString = response.errorBody()?.string()
                    _signUpState.value = SignUpState.Error("Server error (${response.code()}): ${errorBodyString ?: "No specific error message."}")
                    // Log the detailed error for debugging
                    Log.e("SignUpViewModel", "HTTP Error ${response.code()}: $errorBodyString")
                }
            } catch (e: HttpException) {
                // Retrofit specific HTTP errors
                val errorBody = e.response()?.errorBody()?.string()
                _signUpState.value = SignUpState.Error("HTTP Exception: ${e.code()} - ${errorBody ?: e.message}")
                Log.e("SignUpViewModel", "HttpException: ${e.code()} - $errorBody", e)
            } catch (e: IOException) {
                // No internet connection or other network issues
                _signUpState.value = SignUpState.Error("Network error: Could not connect to server. Check your connection and XAMPP.")
                Log.e("SignUpViewModel", "IOException during signup: ${e.message}", e)
            } catch (e: Exception) {
                // Any other unexpected errors
                _signUpState.value = SignUpState.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}")
                Log.e("SignUpViewModel", "Unexpected error during signup: ${e.message}", e)
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }
}