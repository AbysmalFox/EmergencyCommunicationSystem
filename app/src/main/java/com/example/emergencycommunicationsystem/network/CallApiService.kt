package com.example.emergencycommunicationsystem.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class CallEventRequest(
    @SerializedName("call_id") val callId: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("role") val role: String = "user",
    @SerializedName("event") val event: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("duration_sec") val durationSec: Int? = null,
    @SerializedName("location_data") val locationData: Map<String, Any?>? = null,
    @SerializedName("room") val room: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any?>? = null
)

data class CallHistoryItemResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("call_id") val callId: String?,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("role") val role: String?,
    @SerializedName("event") val event: String?,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("duration_sec") val durationSec: Int?,
    @SerializedName("location_data") val locationData: Map<String, Any?>?,
    @SerializedName("room") val room: String?,
    @SerializedName("metadata") val metadata: Map<String, Any?>?,
    @SerializedName("created_at") val createdAt: String?
)

data class CallHistoryPaginationResponse(
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Int
)

data class CallHistoryDataResponse(
    @SerializedName("call_history") val callHistory: List<CallHistoryItemResponse> = emptyList(),
    @SerializedName("pagination") val pagination: CallHistoryPaginationResponse? = null
)

data class CallHistoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CallHistoryDataResponse?
)

data class CallEventResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)

interface CallApiService {
    @GET("user/call_history.php")
    suspend fun getCallHistory(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<CallHistoryResponse>

    @POST("user/call_event.php")
    suspend fun logCallEvent(
        @Body request: CallEventRequest
    ): Response<CallEventResponse>
}

