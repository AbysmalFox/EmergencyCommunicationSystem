package com.example.emergencycommunicationsystem.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object IncidentOutboxSyncScheduler {
    private const val PERIODIC_WORK_NAME = "incident_outbox_periodic_sync"
    private const val ONE_TIME_WORK_NAME = "incident_outbox_immediate_sync"

    private fun connectedConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun schedulePeriodic(context: Context) {
        val work = PeriodicWorkRequestBuilder<IncidentOutboxSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(connectedConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            work
        )
    }

    fun enqueueOneTime(context: Context) {
        val work = OneTimeWorkRequestBuilder<IncidentOutboxSyncWorker>()
            .setConstraints(connectedConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            work
        )
    }
}
