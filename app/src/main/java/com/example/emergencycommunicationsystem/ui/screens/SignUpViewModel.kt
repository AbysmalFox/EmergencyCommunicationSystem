package com.example.emergencycommunicationsystem.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.RegisterRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val message: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        locationPermissionGranted: Boolean,
        latitude: Double?,
        longitude: Double?,
        address: String?
    ) {
        Log.d("SignUpViewModel", "Attempting to sign up user: $email with location permission: $locationPermissionGranted")

        if (password != confirmPassword) {
            _signUpState.value = SignUpState.Error("Passwords do not match.")
            return
        }
        if (fullName.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
            _signUpState.value = SignUpState.Error("All fields are required.")
            return
        }
        if (password.length < 8) {
            _signUpState.value = SignUpState.Error("Password must be at least 8 characters long.")
            return
        }

        _signUpState.value = SignUpState.Loading

        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    name = fullName,
                    email = email,
                    phone = phone,
                    password = password,
                    shareLocation = locationPermissionGranted,
                    latitude = latitude,
                    longitude = longitude,
                    address = address
                )
                val response = authRepository.register(getApplication(), request)

                if (response.success) {
                    Log.i("SignUpViewModel", "Sign-up successful for user: $email")
                    _signUpState.value = SignUpState.Success(response.message)
                } else {
                    Log.w("SignUpViewModel", "Backend rejected sign-up: ${response.message}")
                    _signUpState.value = SignUpState.Error(response.message)
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
                _signUpState.value = SignUpState.Error("HTTP Error ${e.code()}: $errorMsg")
                Log.e("SignUpViewModel", "HttpException: ${e.code()} - $errorBody", e)

            } catch (e: IOException) {
                _signUpState.value = SignUpState.Error("Network error: Could not connect to server. Check your connection.")
                Log.e("SignUpViewModel", "IOException during sign-up: ${e.message}", e)
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("An unexpected error occurred: ${e.localizedMessage}")
                Log.e("SignUpViewModel", "Unexpected error during sign-up", e)
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }
}