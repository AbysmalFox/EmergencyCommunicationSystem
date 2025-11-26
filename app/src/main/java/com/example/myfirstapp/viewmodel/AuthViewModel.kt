package com.example.myfirstapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object SignUpSuccess : AuthState() // New state for successful sign-up
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    val currentUser get() = auth.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AuthViewModel", "Login successful")
                        _authState.value = AuthState.Authenticated
                    } else {
                        Log.e("AuthViewModel", "Login failed", task.exception)
                        _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                    }
                }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Attempting to sign up with email: $email")
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AuthViewModel", "Sign up successful")
                        auth.signOut() // Sign out immediately after sign up
                        _authState.value = AuthState.SignUpSuccess
                    } else {
                        Log.e("AuthViewModel", "Sign up failed", task.exception)
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed")
                    }
                }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun resetAuthState() {
        _authState.value = AuthState.Unauthenticated
    }
}
