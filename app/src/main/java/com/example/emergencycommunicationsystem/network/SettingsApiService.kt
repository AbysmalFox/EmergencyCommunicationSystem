package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.LocationUpdateRequest
import com.example.emergencycommunicationsystem.data.LocationUpdateResponse
import com.example.emergencycommunicationsystem.data.SubscriptionSettingsResponse
import com.example.emergencycommunicationsystem.data.UpdateSubscriptionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SettingsApiService {

    @GET("subscription_settings.php")
    suspend fun getSubscriptionSettings(@Query("user_id") userId: Int): SubscriptionSettingsResponse

    @POST("subscription_settings.php")
    suspend fun updateSubscription(@Body request: UpdateSubscriptionRequest): Response<Unit> // A simple success/fail response

    @POST("update_location.php")
    suspend fun updateUserLocation(@Body request: LocationUpdateRequest): LocationUpdateResponse

}
