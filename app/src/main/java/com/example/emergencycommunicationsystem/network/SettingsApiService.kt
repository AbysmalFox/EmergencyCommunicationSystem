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

data class FcmTokenRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("fcm_token") val fcmToken: String
)

interface SettingsApiService {

    @POST("update_fcm_token.php")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Map<String, Any>>

    @GET("subscription_settings.php")
    suspend fun getSubscriptionSettings(@Query("user_id") userId: Int): SubscriptionSettingsResponse

    @POST("subscription_settings.php")
    suspend fun updateSubscription(@Body request: UpdateSubscriptionRequest): Response<Unit> // A simple success/fail response

    @POST("update_location.php")
    suspend fun updateUserLocation(@Body request: LocationUpdateRequest): LocationUpdateResponse

}
