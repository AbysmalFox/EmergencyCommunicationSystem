package com.example.emergencycommunicationsystem.util

/**
 * Offline fallback for emergency translations.
 * This acts as a "downloaded phrasebook" when online translation is unavailable.
 */
object EmergencyPhrasebook {

    private val phrases = mapOf(
        "Fire Alert" to mapOf(
            "fil" to "Alerto sa Sunog",
            "ceb" to "Alerto sa Sunog",
            "es" to "Alerta de Incendio"
        ),
        "Flood Warning" to mapOf(
            "fil" to "Babala sa Baha",
            "ceb" to "Pahimangno sa Baha",
            "es" to "Advertencia de Inundación"
        ),
        "Typhoon Warning" to mapOf(
            "fil" to "Babala sa Bagyo",
            "ceb" to "Pahimangno sa Bagyo",
            "es" to "Advertencia de Tifón"
        ),
        "Evacuate immediately" to mapOf(
            "fil" to "Lumikas agad",
            "ceb" to "Bakwit dayon",
            "es" to "Evacuar inmediatamente"
        ),
        "Stay indoors" to mapOf(
            "fil" to "Manatili sa loob ng bahay",
            "ceb" to "Pabilin sa sulod sa balay",
            "es" to "Quedarse adentro"
        ),
        "Medical Emergency" to mapOf(
            "fil" to "Emerhensiyang Medikal",
            "ceb" to "Emerhensiyang Medikal",
            "es" to "Emergencia Médica"
        ),
        "Earthquake Alert" to mapOf(
            "fil" to "Alerto sa Lindol",
            "ceb" to "Alerto sa Linog",
            "es" to "Alerta de Terremoto"
        ),
        "Seek higher ground" to mapOf(
            "fil" to "Humanap ng mataas na lugar",
            "ceb" to "Pangita og taas nga lugar",
            "es" to "Busque terreno elevado"
        ),
        "Safe Zone" to mapOf(
            "fil" to "Ligtas na Lugar",
            "ceb" to "Luwas nga Lugar",
            "es" to "Zona Segura"
        )
    )

    fun getTranslation(text: String, targetLanguage: String): String? {
        // Simple case-insensitive exact match for now
        // In a real app, this would use fuzzy matching or token-based lookup
        val key = phrases.keys.find { it.equals(text, ignoreCase = true) } ?: return null
        return phrases[key]?.get(targetLanguage)
    }
}
