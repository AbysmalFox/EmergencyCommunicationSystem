package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.IncidentReportResponse
import com.example.emergencycommunicationsystem.data.UserReportsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IncidentApiService {
    @Multipart
    @POST("report_incident.php")
    suspend fun submitIncident(
        @Part("user_id") userId: RequestBody,
        @Part("incident_type") incidentType: RequestBody,
        @Part("urgency") urgency: RequestBody,
        @Part("details") details: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("address") address: RequestBody?,
        @Part("reporter_name") reporterName: RequestBody?,
        @Part image: MultipartBody.Part?
    ): IncidentReportResponse

    @GET("get_user_reports.php")
    suspend fun getUserReports(@Query("user_id") userId: Int): UserReportsResponse
}