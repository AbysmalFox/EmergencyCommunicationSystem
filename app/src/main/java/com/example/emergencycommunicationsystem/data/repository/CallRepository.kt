package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import android.util.Log
import com.example.emergencycommunicationsystem.data.local.*
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.data.models.*
import com.example.emergencycommunicationsystem.network.CallEventRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository for handling call-related database operations using local Room database
 */
class CallRepository(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val callLogDao = database.callLogDao()
    private val callMessageDao = database.callMessageDao()
    private val userDao = database.userDao()
    
    suspend fun logCall(callLog: CallLog): Result<CallLog> = withContext(Dispatchers.IO) {
        try {
            // Ensure user exists in local database
            ensureUserExists(callLog.userId)
            
            val entity = CallLogEntity(
                userId = callLog.userId,
                callType = callLog.callType,
                startTime = callLog.startTime,
                endTime = callLog.endTime,
                duration = callLog.duration,
                status = callLog.status,
                roomName = callLog.roomName,
                isAdminCall = callLog.isAdminCall,
                createdAt = callLog.createdAt
            )
            
            val id = callLogDao.insertCallLog(entity)
            val loggedCall = callLog.copy(id = id.toInt())

            // Best-effort backend event logging (do not fail local call start).
            logCallEventToBackend(
                callId = callLog.roomName,
                userId = callLog.userId,
                event = "started",
                timestampMillis = callLog.startTime,
                durationSec = null,
                room = callLog.roomName,
                metadata = mapOf(
                    "call_type" to callLog.callType,
                    "is_admin_call" to callLog.isAdminCall
                )
            )
            
            Log.d("CallRepository", "Call logged successfully: ID $id")
            Result.success(loggedCall)
            
        } catch (e: Exception) {
            Log.e("CallRepository", "Error logging call", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateCallDuration(callId: Int, duration: Int, endTime: Long): Result<CallLog> = withContext(Dispatchers.IO) {
        try {
            callLogDao.updateCallDuration(callId, duration, endTime)
            val entity = callLogDao.getCallLogById(callId)
            if (entity != null) {
                val updatedCall = CallLog(
                    id = entity.id,
                    userId = entity.userId,
                    callType = entity.callType,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    duration = entity.duration,
                    status = entity.status,
                    roomName = entity.roomName,
                    isAdminCall = entity.isAdminCall,
                    createdAt = entity.createdAt
                )
                Log.d("CallRepository", "Call duration updated: $duration seconds")
                Result.success(updatedCall)
            } else {
                Result.failure(Exception("Call not found"))
            }
        } catch (e: Exception) {
            Log.e("CallRepository", "Error updating call duration", e)
            Result.failure(e)
        }
    }
    
    suspend fun endCall(callId: Int, endTime: Long, duration: Int): Result<CallLog> = withContext(Dispatchers.IO) {
        try {
            callLogDao.endCall(callId, "ended", endTime, duration)
            val entity = callLogDao.getCallLogById(callId)
            if (entity != null) {
                logCallEventToBackend(
                    callId = entity.roomName,
                    userId = entity.userId,
                    event = "ended",
                    timestampMillis = endTime,
                    durationSec = duration,
                    room = entity.roomName
                )

                val endedCall = CallLog(
                    id = entity.id,
                    userId = entity.userId,
                    callType = entity.callType,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    duration = entity.duration,
                    status = entity.status,
                    roomName = entity.roomName,
                    isAdminCall = entity.isAdminCall,
                    createdAt = entity.createdAt
                )
                Log.d("CallRepository", "Call ended successfully: ID ${endedCall.id}")
                Result.success(endedCall)
            } else {
                Result.failure(Exception("Call not found"))
            }
        } catch (e: Exception) {
            Log.e("CallRepository", "Error ending call", e)
            Result.failure(e)
        }
    }
    
    suspend fun cancelCall(callId: Int, endTime: Long, duration: Int): Result<CallLog> = withContext(Dispatchers.IO) {
        try {
            callLogDao.cancelCall(callId, endTime, duration)
            val entity = callLogDao.getCallLogById(callId)
            if (entity != null) {
                logCallEventToBackend(
                    callId = entity.roomName,
                    userId = entity.userId,
                    event = "cancelled",
                    timestampMillis = endTime,
                    durationSec = duration,
                    room = entity.roomName
                )

                val cancelledCall = CallLog(
                    id = entity.id,
                    userId = entity.userId,
                    callType = entity.callType,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    duration = entity.duration,
                    status = entity.status,
                    roomName = entity.roomName,
                    isAdminCall = entity.isAdminCall,
                    createdAt = entity.createdAt
                )
                Log.d("CallRepository", "Call cancelled successfully: ID ${cancelledCall.id}")
                Result.success(cancelledCall)
            } else {
                Result.failure(Exception("Call not found"))
            }
        } catch (e: Exception) {
            Log.e("CallRepository", "Error cancelling call", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendMessage(callMessage: CallMessage): Result<CallMessage> = withContext(Dispatchers.IO) {
        try {
            val entity = CallMessageEntity(
                callLogId = callMessage.callLogId,
                senderId = callMessage.senderId,
                senderType = callMessage.senderType,
                message = callMessage.message,
                messageType = callMessage.messageType,
                timestamp = callMessage.timestamp,
                isRead = callMessage.isRead
            )
            
            val id = callMessageDao.insertMessage(entity)
            val sentMessage = callMessage.copy(id = id.toInt())
            
            Log.d("CallRepository", "Message sent successfully: ID $id")
            Result.success(sentMessage)
            
        } catch (e: Exception) {
            Log.e("CallRepository", "Error sending message", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCallMessages(callLogId: Int): Result<List<CallMessage>> = withContext(Dispatchers.IO) {
        try {
            val entities = callMessageDao.getMessagesForCallSync(callLogId)
            val messages = entities.map { entity ->
                CallMessage(
                    id = entity.id,
                    callLogId = entity.callLogId,
                    senderId = entity.senderId,
                    senderType = entity.senderType,
                    message = entity.message,
                    messageType = entity.messageType,
                    timestamp = entity.timestamp,
                    isRead = entity.isRead
                )
            }
            
            Log.d("CallRepository", "Retrieved ${messages.size} messages")
            Result.success(messages)
            
        } catch (e: Exception) {
            Log.e("CallRepository", "Error getting messages", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(userId: Int): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val entity = userDao.getUserById(userId)
            if (entity != null) {
                val profile = UserProfile(
                    id = entity.id,
                    username = entity.username,
                    email = entity.email,
                    phone = entity.phone,
                    fullName = entity.fullName,
                    avatar = entity.avatar,
                    isOnline = entity.isOnline,
                    lastSeen = entity.lastSeen
                )
                Log.d("CallRepository", "User profile retrieved: ${profile.username}")
                Result.success(profile)
            } else {
                // Create user if not exists
                val newProfile = createDefaultUser(userId)
                Result.success(newProfile)
            }
        } catch (e: Exception) {
            Log.e("CallRepository", "Error getting user profile", e)
            Result.failure(e)
        }
    }
    
    fun getUserCallLogs(userId: Int): Flow<List<CallLog>> {
        return callLogDao.getUserCallLogs(userId).map { entities ->
            entities.map { entity ->
                CallLog(
                    id = entity.id,
                    userId = entity.userId,
                    callType = entity.callType,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    duration = entity.duration,
                    status = entity.status,
                    roomName = entity.roomName,
                    isAdminCall = entity.isAdminCall,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun getUserCallHistoryRemote(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<CallLog>> = withContext(Dispatchers.IO) {
        try {
            val service = ApiClient.callApiService()
            val response = service.getCallHistory(userId = userId, limit = limit, offset = offset)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code()}"))
            }

            val body = response.body()
            if (body?.success != true) {
                return@withContext Result.failure(Exception(body?.message ?: "Failed to fetch call history"))
            }

            val items = body.data?.callHistory.orEmpty()
            val logs = items.map { item ->
                val timestampSec = item.timestamp ?: 0L
                val timestampMs = if (timestampSec < 1_000_000_000_000L) timestampSec * 1000L else timestampSec
                val event = (item.event ?: "ended").lowercase()
                val status = when (event) {
                    "started", "incoming", "connected" -> "active"
                    "cancelled", "declined", "missed" -> "cancelled"
                    else -> "ended"
                }
                CallLog(
                    id = item.id,
                    userId = item.userId,
                    callType = ((item.metadata?.get("call_type") as? String) ?: "internet"),
                    startTime = timestampMs,
                    endTime = if (status == "active") null else timestampMs,
                    duration = item.durationSec ?: 0,
                    status = status,
                    roomName = item.room ?: item.callId ?: "",
                    isAdminCall = item.role?.equals("admin", ignoreCase = true) == true,
                    createdAt = timestampMs
                )
            }

            Result.success(logs)
        } catch (e: Exception) {
            Log.e("CallRepository", "Error fetching remote call history", e)
            Result.failure(e)
        }
    }
    
    private suspend fun ensureUserExists(userId: Int) {
        val user = userDao.getUserById(userId)
        if (user == null) {
            createDefaultUser(userId)
        }
    }
    
    private suspend fun createDefaultUser(userId: Int): UserProfile {
        val entity = UserEntity(
            id = userId,
            username = "User$userId",
            email = "user$userId@example.com",
            fullName = "Emergency User $userId",
            isOnline = true,
            lastSeen = System.currentTimeMillis()
        )
        
        userDao.insertUser(entity)
        
        return UserProfile(
            id = entity.id,
            username = entity.username,
            email = entity.email,
            phone = entity.phone,
            fullName = entity.fullName,
            avatar = entity.avatar,
            isOnline = entity.isOnline,
            lastSeen = entity.lastSeen
        )
    }

    private suspend fun logCallEventToBackend(
        callId: String,
        userId: Int,
        event: String,
        timestampMillis: Long,
        durationSec: Int? = null,
        room: String? = null,
        metadata: Map<String, Any?>? = null
    ) {
        try {
            val service = ApiClient.callApiService()
            val request = CallEventRequest(
                callId = callId,
                userId = userId,
                event = event,
                timestamp = timestampMillis / 1000L,
                durationSec = durationSec,
                room = room,
                metadata = metadata
            )
            val response = service.logCallEvent(request)
            if (!response.isSuccessful || response.body()?.success != true) {
                Log.w("CallRepository", "Backend call event logging failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.w("CallRepository", "Backend call event logging exception", e)
        }
    }
}
