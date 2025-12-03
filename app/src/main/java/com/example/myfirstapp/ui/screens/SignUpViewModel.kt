package com.example.myfirstapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapp.data.RegisterRequest
import com.example.myfirstapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Define the possible states for the signup UI
sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val message: String) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

class SignUpViewModel : ViewModel() {

    // MutableStateFlow to hold the current UI state, exposed as an immutable StateFlow
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    // Function to trigger the signup process
    fun signUp(username: String, email: String, password: String, confirmPassword: String) {
        // Basic client-side validation before hitting the backend
        if (password != confirmPassword) {
            _signUpState.value = SignUpState.Error("Passwords do not match.")
            return
        }
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _signUpState.value = SignUpState.Error("All fields are required.")
            return
        }
        if (password.length < 6) { // Example password policy
            _signUpState.value = SignUpState.Error("Password must be at least 6 characters long.")
            return
        }
        // More robust email validation can be added here if needed

        _signUpState.value = SignUpState.Loading // Set state to loading

        viewModelScope.launch {
            try {
                // Create the request body
                val request = RegisterRequest(username, email, password)
                // Make the network call
                val response = RetrofitClient.authApiService.registerUser(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true) {
                        _signUpState.value = SignUpState.Success(authResponse.message)
                    } else {
                        // Backend returned success: false
                        _signUpState.value = SignUpState.Error(authResponse?.message ?: "Unknown registration error.")
                    }
                } else {
                    // HTTP error (4xx, 5xx)
                    val errorBody = response.errorBody()?.string()
                    _signUpState.value = SignUpState.Error(errorBody ?: "Network error. Please try again.")
                }
            } catch (e: HttpException) {
                // Retrofit specific HTTP errors
                _signUpState.value = SignUpState.Error("Server error: ${e.message}")
            } catch (e: IOException) {
                // No internet connection or other network issues
                _signUpState.value = SignUpState.Error("Network connection failed. Check your internet and XAMPP server.")
            } catch (e: Exception) {
                // Any other unexpected errors
                _signUpState.value = SignUpState.Error("An unexpected error occurred: ${e.localizedMessage}")
            }
        }
    }

    // Function to reset the state, e.g., after an error message has been shown
    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }
}