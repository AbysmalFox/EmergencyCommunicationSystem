package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import android.net.Uri
import com.example.emergencycommunicationsystem.data.IncidentReportResponse
import com.example.emergencycommunicationsystem.data.UserReportsResponse
import com.example.emergencycommunicationsystem.data.local.AppDatabase
import com.example.emergencycommunicationsystem.data.local.IncidentOutboxEntity
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.data.sync.IncidentOutboxSyncScheduler
import com.example.emergencycommunicationsystem.network.IncidentApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import com.example.emergencycommunicationsystem.util.Resource

class IncidentRepository {

    private suspend fun apiService(): IncidentApiService = ApiClient.incidentApiService()

    suspend fun getUserReports(userId: Int): Resource<UserReportsResponse> {
        return try {
            val response = apiService().getUserReports(userId)
            if (response.success) {
                Resource.Success(response)
            } else {
                Resource.Error(response.message ?: "Failed to fetch reports")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun submitIncident(
        context: Context,
        userId: Int,
        incidentType: String,
        urgency: String,
        details: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        reporterName: String?,
        imageUri: Uri?
    ): IncidentReportResponse {
        val outboxDao = AppDatabase.getDatabase(context).incidentOutboxDao()
        val imagePath = persistImageForOutbox(context, imageUri)
        val attemptResult = trySubmitIncidentNow(
            userId = userId,
            incidentType = incidentType,
            urgency = urgency,
            details = details,
            latitude = latitude,
            longitude = longitude,
            address = address,
            reporterName = reporterName,
            imagePath = imagePath
        )

        return if (attemptResult.success) {
            // Sent immediately: cleanup temp image if any.
            imagePath?.let { File(it).delete() }
            attemptResult
        } else {
            outboxDao.enqueue(
                IncidentOutboxEntity(
                    userId = userId,
                    incidentType = incidentType,
                    urgency = urgency,
                    details = details,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    reporterName = reporterName,
                    imagePath = imagePath,
                    lastError = attemptResult.message
                )
            )
            IncidentOutboxSyncScheduler.enqueueOneTime(context)
            IncidentReportResponse(
                success = true,
                message = "No connection. Report saved and will be sent when back online."
            )
        }
    }

    suspend fun flushOutbox(context: Context, maxItems: Int = 10): Int {
        val outboxDao = AppDatabase.getDatabase(context).incidentOutboxDao()
        val pending = outboxDao.getPending(limit = maxItems)
        var sentCount = 0

        pending.forEach { item ->
            val result = trySubmitIncidentNow(
                userId = item.userId,
                incidentType = item.incidentType,
                urgency = item.urgency,
                details = item.details,
                latitude = item.latitude,
                longitude = item.longitude,
                address = item.address,
                reporterName = item.reporterName,
                imagePath = item.imagePath
            )

            if (result.success) {
                outboxDao.remove(item.id)
                item.imagePath?.let { File(it).delete() }
                sentCount += 1
            } else {
                outboxDao.markAttemptFailed(
                    id = item.id,
                    attemptAt = System.currentTimeMillis(),
                    error = result.message
                )
            }
        }
        return sentCount
    }

    suspend fun getPendingOutboxCount(context: Context): Int {
        return AppDatabase.getDatabase(context).incidentOutboxDao().getPendingCount()
    }

    private suspend fun trySubmitIncidentNow(
        userId: Int,
        incidentType: String,
        urgency: String,
        details: String,
        latitude: Double,
        longitude: Double,
        address: String?,
        reporterName: String?,
        imagePath: String?
    ): IncidentReportResponse {
        return try {
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val incidentTypeBody = incidentType.toRequestBody("text/plain".toMediaTypeOrNull())
            val urgencyBody = urgency.toRequestBody("text/plain".toMediaTypeOrNull())
            val detailsBody = details.toRequestBody("text/plain".toMediaTypeOrNull())
            val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val addressBody = address?.toRequestBody("text/plain".toMediaTypeOrNull())
            val reporterNameBody = reporterName?.toRequestBody("text/plain".toMediaTypeOrNull())

            val imagePart = imagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                } else {
                    null
                }
            }

            apiService().submitIncident(
                userId = userIdBody,
                incidentType = incidentTypeBody,
                urgency = urgencyBody,
                details = detailsBody,
                latitude = latitudeBody,
                longitude = longitudeBody,
                address = addressBody,
                reporterName = reporterNameBody,
                image = imagePart
            )
        } catch (e: Exception) {
            IncidentReportResponse(success = false, message = e.message ?: "Failed to send report")
        }
    }

    private fun persistImageForOutbox(context: Context, imageUri: Uri?): String? {
        if (imageUri == null) return null
        return try {
            val outDir = File(context.filesDir, "incident_outbox_images")
            if (!outDir.exists()) outDir.mkdirs()
            val file = File(outDir, "incident_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
