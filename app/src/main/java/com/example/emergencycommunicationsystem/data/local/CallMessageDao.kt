package com.example.emergencycommunicationsystem.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallMessageDao {
    
    @Query("SELECT * FROM call_messages WHERE id = :id")
    suspend fun getMessageById(id: Int): CallMessageEntity?
    
    @Query("SELECT * FROM call_messages WHERE callLogId = :callLogId ORDER BY timestamp ASC")
    fun getMessagesForCall(callLogId: Int): Flow<List<CallMessageEntity>>
    
    @Query("SELECT * FROM call_messages WHERE callLogId = :callLogId ORDER BY timestamp ASC")
    suspend fun getMessagesForCallSync(callLogId: Int): List<CallMessageEntity>
    
    @Query("SELECT * FROM call_messages WHERE callLogId = :callLogId AND isRead = 0")
    suspend fun getUnreadMessagesForCall(callLogId: Int): List<CallMessageEntity>
    
    @Insert
    suspend fun insertMessage(message: CallMessageEntity): Long
    
    @Insert
    suspend fun insertMessages(messages: List<CallMessageEntity>)
    
    @Update
    suspend fun updateMessage(message: CallMessageEntity)
    
    @Query("UPDATE call_messages SET isRead = 1 WHERE callLogId = :callLogId")
    suspend fun markAllMessagesAsRead(callLogId: Int)
    
    @Query("UPDATE call_messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Int)
    
    @Query("DELETE FROM call_messages WHERE callLogId = :callLogId")
    suspend fun deleteMessagesForCall(callLogId: Int)
    
    @Query("DELETE FROM call_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Int)
    
    @Query("SELECT COUNT(*) FROM call_messages WHERE callLogId = :callLogId")
    suspend fun getMessageCountForCall(callLogId: Int): Int
}
