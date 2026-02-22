package com.example.emergencycommunicationsystem.ui.screens

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.RegisterRequest
import com.example.emergencycommunicationsystem.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class SignUpOtpState(
    val emailForOtp: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false,
    val isVerified: Boolean = false,
    val canResendInSeconds: Int = 0,
    val message: String? = null
)

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState
    private val _otpState = MutableStateFlow(SignUpOtpState())
    val otpState: StateFlow<SignUpOtpState> = _otpState

    private var verificationToken: String? = null
    private var resendCountdownJob: Job? = null

    fun onEmailChanged(email: String) {
        val trimmed = email.trim()
        if (_otpState.value.emailForOtp != trimmed) {
            Log.d("SignUpViewModel", "Email changed. Resetting OTP state.")
            resetOtpState(message = null)
        }
    }

    fun sendOtp(email: String) {
        val trimmedEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() || !trimmedEmail.endsWith("@gmail.com", ignoreCase = true)) {
            Log.w("SignUpViewModel", "sendOtp rejected: invalid Gmail address: $trimmedEmail")
            _otpState.value = _otpState.value.copy(message = "Enter a valid Gmail address first.")
            return
        }
        if (_otpState.value.isSending) return
        if (_otpState.value.canResendInSeconds > 0) {
            Log.w("SignUpViewModel", "sendOtp rate-limited: ${_otpState.value.canResendInSeconds}s remaining for $trimmedEmail")
            _otpState.value = _otpState.value.copy(message = "Please wait ${_otpState.value.canResendInSeconds}s before resending OTP.")
            return
        }

        _otpState.value = _otpState.value.copy(
            emailForOtp = trimmedEmail,
            isSending = true,
            isSent = false,
            isVerified = false,
            message = null
        )

        viewModelScope.launch {
            try {
                Log.d("SignUpViewModel", "Requesting signup OTP for $trimmedEmail")
                val response = authRepository.requestEmailOtp(trimmedEmail)
                if (response.success) {
                    verificationToken = null
                    Log.i("SignUpViewModel", "OTP requested successfully for $trimmedEmail")
                    _otpState.value = _otpState.value.copy(
                        isSending = false,
                        isSent = true,
                        isVerified = false,
                        message = response.message.ifBlank { "OTP sent to $trimmedEmail" }
                    )
                    startResendCountdown(60)
                } else {
                    Log.w("SignUpViewModel", "OTP request rejected by backend for $trimmedEmail: ${response.message}")
                    _otpState.value = _otpState.value.copy(
                        isSending = false,
                        isSent = false,
                        isVerified = false,
                        message = response.message.ifBlank { "Failed to send OTP." }
                    )
                }
            } catch (e: HttpException) {
                Log.e("SignUpViewModel", "OTP request HTTP error ${e.code()} for $trimmedEmail", e)
                val message = when (e.code()) {
                    404 -> "OTP endpoint not found on server (request_email_otp.php)."
                    429 -> "Too many OTP requests. Please wait and try again."
                    500 -> "Server error while sending OTP."
                    else -> "OTP request failed (HTTP ${e.code()})."
                }
                _otpState.value = _otpState.value.copy(
                    isSending = false,
                    isSent = false,
                    isVerified = false,
                    message = message
                )
            } catch (e: Exception) {
                Log.e("SignUpViewModel", "OTP request failed for $trimmedEmail", e)
                _otpState.value = _otpState.value.copy(
                    isSending = false,
                    isSent = false,
                    isVerified = false,
                    message = "Could not send OTP. Please try again."
                )
            }
        }
    }

    fun verifyOtp(email: String, inputOtp: String) {
        val trimmedEmail = email.trim()
        val cleanOtp = inputOtp.trim()

        if (trimmedEmail != _otpState.value.emailForOtp) {
            Log.w("SignUpViewModel", "verifyOtp rejected: email changed from ${_otpState.value.emailForOtp} to $trimmedEmail")
            _otpState.value = _otpState.value.copy(
                isVerified = false,
                message = "Email changed. Please request a new OTP."
            )
            return
        }
        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            Log.w("SignUpViewModel", "verifyOtp rejected: invalid OTP format for $trimmedEmail")
            _otpState.value = _otpState.value.copy(message = "OTP must be 6 digits.")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("SignUpViewModel", "Verifying OTP for $trimmedEmail")
                val response = authRepository.verifyEmailOtp(trimmedEmail, cleanOtp)
                if (response.success) {
                    verificationToken = response.verificationToken
                    Log.i("SignUpViewModel", "OTP verified for $trimmedEmail. Token present=${!verificationToken.isNullOrBlank()}")
                    _otpState.value = _otpState.value.copy(
                        isVerified = true,
                        message = response.message.ifBlank { "Email verified successfully." }
                    )
                } else {
                    verificationToken = null
                    Log.w("SignUpViewModel", "OTP verification rejected by backend for $trimmedEmail: ${response.message}")
                    _otpState.value = _otpState.value.copy(
                        isVerified = false,
                        message = response.message.ifBlank { "Incorrect OTP." }
                    )
                }
            } catch (e: HttpException) {
                verificationToken = null
                Log.e("SignUpViewModel", "OTP verification HTTP error ${e.code()} for $trimmedEmail", e)
                val message = when (e.code()) {
                    404 -> "OTP verification endpoint not found on server (verify_email_otp.php)."
                    400 -> "Invalid or expired OTP."
                    429 -> "Too many verification attempts. Please try again later."
                    500 -> "Server error while verifying OTP."
                    else -> "OTP verification failed (HTTP ${e.code()})."
                }
                _otpState.value = _otpState.value.copy(
                    isVerified = false,
                    message = message
                )
            } catch (e: Exception) {
                verificationToken = null
                Log.e("SignUpViewModel", "OTP verification failed for $trimmedEmail", e)
                _otpState.value = _otpState.value.copy(
                    isVerified = false,
                    message = "OTP verification failed. Please try again."
                )
            }
        }
    }

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
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || !email.endsWith("@gmail.com", ignoreCase = true)) {
            Log.w("SignUpViewModel", "signUp rejected: invalid Gmail $email")
            _signUpState.value = SignUpState.Error("Please enter a valid Gmail address.")
            return
        }
        if (!_otpState.value.isVerified || _otpState.value.emailForOtp != email.trim()) {
            Log.w("SignUpViewModel", "signUp rejected: OTP not verified for $email")
            _signUpState.value = SignUpState.Error("Please verify your Gmail OTP before signing up.")
            return
        }
        if (verificationToken.isNullOrBlank()) {
            Log.w("SignUpViewModel", "signUp rejected: verification token missing for $email")
            _signUpState.value = SignUpState.Error("Verification token is missing. Verify OTP again.")
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
                    address = address,
                    emailVerificationToken = verificationToken
                )
                val response = authRepository.register(getApplication(), request)

                if (response.success) {
                    Log.i("SignUpViewModel", "Sign-up successful for user: $email")
                    _signUpState.value = SignUpState.Success(response.message)
                    resetOtpState(message = null)
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

    private fun resetOtpState(message: String?) {
        Log.d("SignUpViewModel", "Resetting OTP state. message=$message")
        verificationToken = null
        resendCountdownJob?.cancel()
        _otpState.value = SignUpOtpState(message = message)
    }

    private fun startResendCountdown(seconds: Int) {
        resendCountdownJob?.cancel()
        resendCountdownJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _otpState.value = _otpState.value.copy(canResendInSeconds = remaining)
                delay(1000)
            }
            _otpState.value = _otpState.value.copy(canResendInSeconds = 0)
        }
    }
}
