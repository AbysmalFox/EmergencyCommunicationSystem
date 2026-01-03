package com.example.emergencycommunicationsystem.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.RegisterRequest
import com.example.emergencycommunicationsystem.data.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    // Updated Success state to include user data and location permission
    data class Success(
        val message: String,
        val userId: Int,
        val username: String,
        val email: String,
        val token: String,
        val locationPermissionGranted: Boolean
    ) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(fullName: String, email: String, phone: String, password: String, confirmPassword: String, locationPermissionGranted: Boolean) {
        Log.d("SignUpViewModel", "Received fullName: '$fullName'")
        Log.d("SignUpViewModel", "Received email: '$email'")
        Log.d("SignUpViewModel", "Received phone: '$phone'")
        Log.d("SignUpViewModel", "Location permission granted: $locationPermissionGranted")

        if (password != confirmPassword) {
            _signUpState.value = SignUpState.Error("Passwords do not match.")
            return
        }
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
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
                val request = RegisterRequest(
                    name = fullName,
                    email = email,
                    phone = phone,
                    password = password
                )
                val response = ApiClient.authApiService.registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.userId != null && authResponse.user != null && authResponse.token != null) {
                        _signUpState.value = SignUpState.Success(
                            message = authResponse.message,
                            userId = authResponse.userId,
                            username = authResponse.user.name,
                            email = authResponse.user.email,
                            token = authResponse.token,
                            locationPermissionGranted = locationPermissionGranted
                        )
                    } else {
                        _signUpState.value = SignUpState.Error(authResponse?.message ?: "Registration failed: Unknown reason.")
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    _signUpState.value = SignUpState.Error("Server error (${response.code()}): ${errorBodyString ?: "No specific error message."}")
                    Log.e("SignUpViewModel", "HTTP Error ${response.code()}: $errorBodyString")
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _signUpState.value = SignUpState.Error("HTTP Exception: ${e.code()} - ${errorBody ?: e.message}")
                Log.e("SignUpViewModel", "HttpException: ${e.code()} - $errorBody", e)
            } catch (e: IOException) {
                _signUpState.value = SignUpState.Error("Network error: Could not connect to server. Check your connection and XAMPP.")
                Log.e("SignUpViewModel", "IOException during signup: ${e.message}", e)
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}")
                Log.e("SignUpViewModel", "Unexpected error during signup: ${e.message}", e)
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }
}