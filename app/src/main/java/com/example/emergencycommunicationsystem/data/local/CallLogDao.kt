package com.example.emergencycommunicationsystem.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    
    @Query("SELECT * FROM call_logs WHERE id = :id")
    suspend fun getCallLogById(id: Int): CallLogEntity?
    
    @Query("SELECT * FROM call_logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserCallLogs(userId: Int): Flow<List<CallLogEntity>>
    
    @Query("SELECT * FROM call_logs WHERE userId = :userId AND status = 'active' LIMIT 1")
    suspend fun getActiveCallForUser(userId: Int): CallLogEntity?
    
    @Query("SELECT * FROM call_logs WHERE roomName = :roomName LIMIT 1")
    suspend fun getCallByRoomName(roomName: String): CallLogEntity?
    
    @Insert
    suspend fun insertCallLog(callLog: CallLogEntity): Long
    
    @Update
    suspend fun updateCallLog(callLog: CallLogEntity)
    
    @Query("UPDATE call_logs SET duration = :duration, endTime = :endTime WHERE id = :id")
    suspend fun updateCallDuration(id: Int, duration: Int, endTime: Long)
    
    @Query("UPDATE call_logs SET status = :status, endTime = :endTime, duration = :duration WHERE id = :id")
    suspend fun endCall(id: Int, status: String, endTime: Long, duration: Int)
    
    @Query("UPDATE call_logs SET status = 'cancelled', endTime = :endTime, duration = :duration WHERE id = :id")
    suspend fun cancelCall(id: Int, endTime: Long, duration: Int)
    
    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLog(id: Int)
    
    @Query("SELECT COUNT(*) FROM call_logs WHERE userId = :userId AND status = 'ended'")
    suspend fun getUserCompletedCallsCount(userId: Int): Int
    
    @Query("SELECT SUM(duration) FROM call_logs WHERE userId = :userId AND status = 'ended'")
    suspend fun getUserTotalCallDuration(userId: Int): Long?
}
