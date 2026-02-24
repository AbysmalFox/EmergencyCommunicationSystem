package com.example.emergencycommunicationsystem.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.emergencycommunicationsystem.data.repository.IncidentRepository

class IncidentOutboxSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = IncidentRepository()
            repository.flushOutbox(applicationContext, maxItems = 20)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
