package com.example.emergencycommunicationsystem.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emergencycommunicationsystem.data.models.*
import com.example.emergencycommunicationsystem.data.repository.CallRepository
import com.example.emergencycommunicationsystem.webrtc.SignalingManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class InternetCallViewModel(
    private val callRepository: CallRepository,
    private val signalingManager: SignalingManager? = null
) : ViewModel() {
    
    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    private val _messageInput = mutableStateOf("")
    val messageInput: State<String> = _messageInput
    
    private val _isSending = mutableStateOf(false)
    val isSending: State<Boolean> = _isSending
    
    private var currentCallLog: CallLog? = null
    private var timerJob: Job? = null
    
    init {
        // Initialize call state
        _callState.value = CallState()
    }
    
    fun startCall(userId: Int, roomName: String) {
        viewModelScope.launch {
            try {
                // Update state to connecting
                _callState.update { it.copy(
                    isConnecting = true,
                    roomName = roomName,
                    startTime = System.currentTimeMillis()
                ) }
                
                // Get user profile
                val profileResult = callRepository.getUserProfile(userId)
                if (profileResult.isSuccess) {
                    _callState.update { it.copy(
                        remoteUser = profileResult.getOrNull()
                    ) }
                }
                
                // Log the call
                val callLog = CallLog(
                    userId = userId,
                    callType = "internet",
                    startTime = System.currentTimeMillis(),
                    status = "active",
                    roomName = roomName
                )
                
                val logResult = callRepository.logCall(callLog)
                if (logResult.isSuccess) {
                    currentCallLog = logResult.getOrNull()
                    
                    // Start WebRTC connection
                    signalingManager?.connect(roomName)
                    
                    // Update state to active
                    _callState.update { it.copy(
                        isActive = true,
                        isConnecting = false
                    ) }
                    
                    // Start timer
                    startTimer()
                    
                    Log.d("InternetCallViewModel", "Call started successfully")
                } else {
                    Log.e("InternetCallViewModel", "Failed to log call")
                    _callState.update { it.copy(isConnecting = false) }
                }
                
            } catch (e: Exception) {
                Log.e("InternetCallViewModel", "Error starting call", e)
                _callState.update { it.copy(isConnecting = false) }
            }
        }
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && _callState.value.isActive) {
                val currentTime = System.currentTimeMillis()
                val elapsedSeconds = ((currentTime - startTime) / 1000).toInt()
                _callState.update { it.copy(duration = elapsedSeconds) }
                
                // Update call duration in database every 10 seconds
                if (elapsedSeconds % 10 == 0 && currentCallLog != null) {
                    callRepository.updateCallDuration(
                        currentCallLog!!.id,
                        elapsedSeconds,
                        currentTime
                    )
                }
                
                delay(1000)
            }
        }
    }
    
    fun endCall() {
        viewModelScope.launch {
            try {
                timerJob?.cancel()
                
                val endTime = System.currentTimeMillis()
                val duration = _callState.value.duration
                
                // Update call state
                _callState.update { it.copy(
                    isActive = false,
                    isConnecting = false
                ) }
                
                // End call in database
                currentCallLog?.let { callLog ->
                    callRepository.endCall(callLog.id, endTime, duration)
                }
                
                // Disconnect WebRTC
                signalingManager?.disconnect()
                
                Log.d("InternetCallViewModel", "Call ended successfully")
                
            } catch (e: Exception) {
                Log.e("InternetCallViewModel", "Error ending call", e)
            }
        }
    }
    
    fun cancelCall() {
        viewModelScope.launch {
            try {
                timerJob?.cancel()
                
                val endTime = System.currentTimeMillis()
                val duration = _callState.value.duration
                
                // Update call state
                _callState.update { it.copy(
                    isActive = false,
                    isConnecting = false
                ) }
                
                // Update call status to cancelled in database
                currentCallLog?.let { callLog ->
                    callRepository.endCall(callLog.id, endTime, duration)
                }
                
                // Disconnect WebRTC
                signalingManager?.disconnect()
                
                Log.d("InternetCallViewModel", "Call cancelled")
                
            } catch (e: Exception) {
                Log.e("InternetCallViewModel", "Error cancelling call", e)
            }
        }
    }
    
    fun updateMessageInput(message: String) {
        _messageInput.value = message
    }
    
    fun sendMessage() {
        if (_messageInput.value.isBlank() || currentCallLog == null) return
        
        viewModelScope.launch {
            try {
                _isSending.value = true
                
                val callMessage = CallMessage(
                    callLogId = currentCallLog!!.id,
                    senderId = com.example.emergencycommunicationsystem.AuthManager.getUserId(),
                    senderType = "user",
                    message = _messageInput.value.trim(),
                    messageType = "text"
                )
                
                val result = callRepository.sendMessage(callMessage)
                val sentMessage = result.getOrNull()
                if (result.isSuccess && sentMessage != null) {
                    // Clear input
                    _messageInput.value = ""
                    
                    // Add to local messages list
                    _callState.update { it.copy(
                        messages = it.messages + sentMessage
                    ) }
                    
                    Log.d("InternetCallViewModel", "Message sent successfully")
                } else {
                    Log.e("InternetCallViewModel", "Failed to send message")
                }
                
            } catch (e: Exception) {
                Log.e("InternetCallViewModel", "Error sending message", e)
            } finally {
                _isSending.value = false
            }
        }
    }
    
    fun toggleMute() {
        _callState.update { it.copy(isMuted = !it.isMuted) }
    }
    
    fun toggleSpeaker() {
        _callState.update { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        endCall()
    }
}
