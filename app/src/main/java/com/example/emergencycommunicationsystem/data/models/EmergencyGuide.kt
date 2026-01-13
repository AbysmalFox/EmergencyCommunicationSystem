package com.example.emergencycommunicationsystem.data.models

/**
 * Data model for emergency guide types
 */
data class EmergencyGuide(
    val id: String,
    val title: String,
    val category: EmergencyCategory,
    val icon: String, // Emoji or icon identifier
    val description: String,
    val tips: List<EmergencyTip>
)

/**
 * Emergency categories
 */
enum class EmergencyCategory {
    MEDICAL,
    NATURAL_DISASTER,
    CRIME,
    ACCIDENT,
    FIRE,
    WEATHER
}

/**
 * Individual tip/instruction for an emergency
 */
data class EmergencyTip(
    val title: String,
    val description: String,
    val priority: TipPriority = TipPriority.NORMAL
)

enum class TipPriority {
    CRITICAL, // Must do immediately
    HIGH,     // Very important
    NORMAL    // General advice
}
