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
    // Simplified Success state - the detailed user data is no longer needed here.
    data class Success(val message: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun signUp(fullName: String, email: String, phone: String, password: String, confirmPassword: String, locationPermissionGranted: Boolean) {
        Log.d("SignUpViewModel", "Attempting to sign up user: $email with location permission: $locationPermissionGranted")

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
                    password = password,
                    shareLocation = locationPermissionGranted
                )
                val response = ApiClient.authApiService.registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    // Simplified the success check. We only care if the backend confirms success.
                    if (authResponse?.success == true) {
                        Log.i("SignUpViewModel", "Sign-up successful for user: $email")
                        _signUpState.value = SignUpState.Success(authResponse.message)
                    } else {
                        val backendMessage = authResponse?.message ?: "Registration failed: Unknown reason from backend."
                        Log.w("SignUpViewModel", "Backend rejected sign-up: $backendMessage")
                        _signUpState.value = SignUpState.Error(backendMessage)
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string() ?: "No error body"
                    Log.e("SignUpViewModel", "HTTP Error ${response.code()}: $errorBodyString")
                    if (response.code() in 500..599) {
                        Log.e("SignUpViewModel", "This is a server-side error. Check your PHP/Apache 'error.log' in XAMPP for the detailed PDOException message.")
                        _signUpState.value = SignUpState.Error("Server error (${response.code()}): Could not complete registration. Check Logcat for guidance.")
                    } else {
                        _signUpState.value = SignUpState.Error("Server error (${response.code()}): $errorBodyString")
                    }
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("SignUpViewModel", "HttpException: ${e.code()} - $errorBody", e)
                _signUpState.value = SignUpState.Error("Network error: ${e.code()}. See Logcat for details.")
            } catch (e: IOException) {
                Log.e("SignUpViewModel", "IOException during sign-up. Check network connection and XAMPP server status.", e)
                _signUpState.value = SignUpState.Error("Network error: Could not connect to the server.")
            } catch (e: Exception) {
                Log.e("SignUpViewModel", "An unexpected error occurred during sign-up.", e)
                _signUpState.value = SignUpState.Error("An unexpected error occurred: ${e.localizedMessage}")
            }
        }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }
}