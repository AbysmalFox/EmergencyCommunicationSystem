package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import android.net.Uri
import com.example.emergencycommunicationsystem.data.IncidentReportResponse
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.IncidentApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class IncidentRepository {

    private val apiService: IncidentApiService = ApiClient.incidentApiService

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

        val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val incidentTypeBody = incidentType.toRequestBody("text/plain".toMediaTypeOrNull())
        val urgencyBody = urgency.toRequestBody("text/plain".toMediaTypeOrNull())
        val detailsBody = details.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val addressBody = address?.toRequestBody("text/plain".toMediaTypeOrNull())
        val reporterNameBody = reporterName?.toRequestBody("text/plain".toMediaTypeOrNull())

        var imagePart: MultipartBody.Part? = null
        imageUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                val file = File(context.cacheDir, "incident_image.jpg")
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            }
        }

        return apiService.submitIncident(
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
    }
}