package com.example.emergencycommunicationsystem.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Unauthenticated : AuthState() // The initial state, prompting for action
    object Loading : AuthState()
    object SignUpSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(getInitialState())
    val authState: StateFlow<AuthState> = _authState

    private fun getInitialState(): AuthState {
        return AuthState.Unauthenticated
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Error("Login functionality is not available without a backend.")
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Error("Sign up functionality is not available without a backend.")
        }
    }

    fun logout() {
        _authState.value = AuthState.Unauthenticated
    }

    fun resetAuthState() {
        // After SignUpSuccess or Error, go back to the prompt screen
        _authState.value = AuthState.Unauthenticated
    }
}
