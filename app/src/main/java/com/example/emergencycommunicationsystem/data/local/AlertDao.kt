package com.example.emergencycommunicationsystem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isAcknowledged = 0")
    fun getUnacknowledgedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<AlertEntity>)

    @Query("DELETE FROM alerts")
    suspend fun clearAlerts()

    @Query("UPDATE alerts SET isAcknowledged = 1 WHERE id = :alertId")
    suspend fun updateAcknowledgeStatus(alertId: Int)

    @Query("UPDATE alerts SET isAcknowledged = 0 WHERE id = :alertId")
    suspend fun revertAcknowledgeStatus(alertId: Int)
}
