package com.example.emergencycommunicationsystem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IncidentOutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: IncidentOutboxEntity): Long

    @Query("SELECT * FROM incident_outbox ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getPending(limit: Int = 10): List<IncidentOutboxEntity>

    @Query("DELETE FROM incident_outbox WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("SELECT COUNT(*) FROM incident_outbox")
    suspend fun getPendingCount(): Int

    @Query(
        "UPDATE incident_outbox SET retryCount = retryCount + 1, lastAttemptAt = :attemptAt, lastError = :error WHERE id = :id"
    )
    suspend fun markAttemptFailed(id: Long, attemptAt: Long, error: String?)
}
