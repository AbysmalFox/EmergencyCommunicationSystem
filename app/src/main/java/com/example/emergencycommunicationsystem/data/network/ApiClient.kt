package com.example.emergencycommunicationsystem.data.network

import android.util.Log
import com.example.emergencycommunicationsystem.BuildConfig
import com.example.emergencycommunicationsystem.network.AlertsApiService
import com.example.emergencycommunicationsystem.network.AuthApiService
import com.example.emergencycommunicationsystem.network.IncidentApiService
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SettingsApiService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val _useLocalServer = MutableStateFlow(false)
    private val isInitialized = CompletableDeferred<Unit>()

    private val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    private val gsonConverterFactory = GsonConverterFactory.create()

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

    // --- Service Providers are now suspend functions ---
    suspend fun authApiService(): AuthApiService {
        isInitialized.await()
        return if (_useLocalServer.value) localRetrofit.create(AuthApiService::class.java)
        else productionRetrofit.create(AuthApiService::class.java)
    }

    suspend fun alertsApiService(): AlertsApiService {
        isInitialized.await()
        return if (_useLocalServer.value) localRetrofit.create(AlertsApiService::class.java)
        else productionRetrofit.create(AlertsApiService::class.java)
    }

    suspend fun messagingApiService(): MessagingApiService {
        isInitialized.await()
        return if (_useLocalServer.value) localRetrofit.create(MessagingApiService::class.java)
        else productionRetrofit.create(MessagingApiService::class.java)
    }

    suspend fun settingsApiService(): SettingsApiService {
        isInitialized.await()
        return if (_useLocalServer.value) localRetrofit.create(SettingsApiService::class.java)
        else productionRetrofit.create(SettingsApiService::class.java)
    }

    suspend fun incidentApiService(): IncidentApiService {
        isInitialized.await()
        return if (_useLocalServer.value) localRetrofit.create(IncidentApiService::class.java)
        else productionRetrofit.create(IncidentApiService::class.java)
    }

    fun initializeAndCheckConnection() {
        if (isInitialized.isCompleted) return

        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder()
                        .url(NetworkConfig.PRODUCTION_API_URL + "alerts.php")
                        .head()
                        .build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        _useLocalServer.value = false
                        Log.i("ApiClient", "Production server is reachable. Using ONLINE mode.")
                    } else {
                        _useLocalServer.value = true
                        Log.w("ApiClient", "Production server returned ${response.code}. Falling back to LOCAL mode.")
                    }
                } catch (e: Exception) {
                    _useLocalServer.value = true
                    Log.e("ApiClient", "Production server unreachable. Falling back to LOCAL mode.", e)
                } finally {
                    isInitialized.complete(Unit)
                }
            }
        } else {
            _useLocalServer.value = false
            isInitialized.complete(Unit)
        }
    }
}