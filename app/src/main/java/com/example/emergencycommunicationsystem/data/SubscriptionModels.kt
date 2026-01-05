package com.example.emergencycommunicationsystem.data

import com.google.gson.annotations.SerializedName

/**
 * Represents a single notification subscription category from the backend.
 */
data class SubscriptionCategory(
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val icon: String? = null,
    val description: String? = null,
    @SerializedName("is_subscribed")
    val isSubscribed: Int // Backend uses 1 for true, 0 for false
)

/**
 * Represents the entire response from the GET subscription_settings.php endpoint.
 */
data class SubscriptionSettingsResponse(
    val success: Boolean,
    val data: List<SubscriptionCategory>
)

/**
 * Represents the request body for updating a subscription status.
 */
data class UpdateSubscriptionRequest(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("is_active")
    val isActive: Int
)
