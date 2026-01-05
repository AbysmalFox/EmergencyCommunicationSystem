package com.example.emergencycommunicationsystem.data.network

import com.example.emergencycommunicationsystem.BuildConfig
import com.example.emergencycommunicationsystem.network.AlertsApiService
import com.example.emergencycommunicationsystem.network.AuthApiService
import com.example.emergencycommunicationsystem.network.IncidentApiService
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SettingsApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

/**
 * Manages API services and implements the dynamic fallback from production to local.
 */
object ApiClient {
    // State to track if we should use the local server as a fallback
    private val _useLocalServer = MutableStateFlow(false)

    // --- Client and Factory Setup ---
    private val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    private val gsonConverterFactory = GsonConverterFactory.create()

    // --- Retrofit Instances for Both Environments ---
    private val productionRetrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.PRODUCTION_API_URL)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()

    private val localRetrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.LOCAL_API_URL)
        .client(okHttpClient)
        .addConverterFactory(gsonConverterFactory)
        .build()

    // --- Dynamic Service Providers ---
    // These getters check the fallback state *every time* a service is requested.
    val authApiService: AuthApiService
        get() = if (_useLocalServer.value) localRetrofit.create(AuthApiService::class.java)
                else productionRetrofit.create(AuthApiService::class.java)

    val alertsApiService: AlertsApiService
        get() = if (_useLocalServer.value) localRetrofit.create(AlertsApiService::class.java)
                else productionRetrofit.create(AlertsApiService::class.java)

    val messagingApiService: MessagingApiService
        get() = if (_useLocalServer.value) localRetrofit.create(MessagingApiService::class.java)
                else productionRetrofit.create(MessagingApiService::class.java)

    val settingsApiService: SettingsApiService
        get() = if (_useLocalServer.value) localRetrofit.create(SettingsApiService::class.java)
        else productionRetrofit.create(SettingsApiService::class.java)

    val incidentApiService: IncidentApiService
        get() = if (_useLocalServer.value) localRetrofit.create(IncidentApiService::class.java)
        else productionRetrofit.create(IncidentApiService::class.java)

    /**
     * Pings the production server to check for connectivity at app startup.
     * If it fails, it sets the flag to use the local server as a fallback.
     */
    fun initializeAndCheckConnection() {
        // Only run this logic in DEBUG builds. Release builds will always use production.
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val host = NetworkConfig.PRODUCTION_HOST.removePrefix("https://").removeSuffix("/")
                    // A quick DNS lookup is enough to check for internet connectivity.
                    InetAddress.getByName(host)
                    _useLocalServer.value = false // Success, use production server
                } catch (e: Exception) {
                    // Failure (no internet, DNS issue), fall back to local server
                    _useLocalServer.value = true
                }
            }
        } else {
            // For RELEASE builds, never fall back to local.
            _useLocalServer.value = false
        }
    }
}