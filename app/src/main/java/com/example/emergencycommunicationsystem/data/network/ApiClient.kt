package com.example.emergencycommunicationsystem.data.network

import android.content.Context
import android.util.Log
import com.example.emergencycommunicationsystem.network.AlertsApiService
import com.example.emergencycommunicationsystem.network.AuthApiService
import com.example.emergencycommunicationsystem.network.IncidentApiService
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SettingsApiService
import com.example.emergencycommunicationsystem.util.NetworkUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.emergencycommunicationsystem.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ApiClient {
    private val _useLocalServer = MutableStateFlow(false)
    private val isInitialized = CompletableDeferred<Unit>()

    @Volatile
    private var okHttpClient: OkHttpClient? = null
    @Volatile
    private var productionRetrofit: Retrofit? = null
    @Volatile
    private var localRetrofit: Retrofit? = null
    
    private val gsonConverterFactory = GsonConverterFactory.create()

    private fun getOkHttpClient(context: Context? = null): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: buildOkHttpClient(context).also { okHttpClient = it }
        }
    }

    private fun buildOkHttpClient(context: Context?): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AlertaraQC/1.0")
                    .header("Accept", "application/json")
                    .header("Connection", "close")
                    .build()
                chain.proceed(request)
            }
        
        context?.let { ctx ->
            val cacheSize = 10 * 1024 * 1024L 
            val cacheDir = File(ctx.cacheDir, "http_cache")
            val cache = Cache(cacheDir, cacheSize)
            builder.cache(cache)
        }
        return builder.build()
    }

    private fun getRetrofit(): Retrofit {
        val useLocal = if (!BuildConfig.DEBUG) false else _useLocalServer.value
        
        return if (useLocal) {
            localRetrofit ?: synchronized(this) {
                localRetrofit ?: Retrofit.Builder()
                    .baseUrl(NetworkConfig.LOCAL_API_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(gsonConverterFactory)
                    .build().also { localRetrofit = it }
            }
        } else {
            productionRetrofit ?: synchronized(this) {
                productionRetrofit ?: Retrofit.Builder()
                    .baseUrl(NetworkConfig.PRODUCTION_API_URL)
                    .client(getOkHttpClient())
                    .addConverterFactory(gsonConverterFactory)
                    .build().also { productionRetrofit = it }
            }
        }
    }

    suspend fun authApiService(): AuthApiService {
        isInitialized.await()
        return getRetrofit().create(AuthApiService::class.java)
    }

    suspend fun alertsApiService(): AlertsApiService {
        isInitialized.await()
        return getRetrofit().create(AlertsApiService::class.java)
    }

    suspend fun messagingApiService(): MessagingApiService {
        isInitialized.await()
        return getRetrofit().create(MessagingApiService::class.java)
    }

    suspend fun settingsApiService(): SettingsApiService {
        isInitialized.await()
        return getRetrofit().create(SettingsApiService::class.java)
    }

    suspend fun incidentApiService(): IncidentApiService {
        isInitialized.await()
        return getRetrofit().create(IncidentApiService::class.java)
    }

    fun initializeAndCheckConnection(context: Context) {
        if (isInitialized.isCompleted) return
        val appContext = context.applicationContext
        getOkHttpClient(appContext)

        if (BuildConfig.ALLOW_LOCAL_FALLBACK && BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder()
                        .url(NetworkConfig.PRODUCTION_API_URL + "alerts.php")
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile) AlertaraQC/1.0")
                        .head()
                        .build()
                    
                    val response = withContext(Dispatchers.IO) {
                        getOkHttpClient().newCall(request).execute()
                    }
                    
                    _useLocalServer.value = !response.isSuccessful
                } catch (e: Exception) {
                    _useLocalServer.value = true
                    Log.e("ApiClient", "Connection check failed", e)
                } finally {
                    if (!isInitialized.isCompleted) isInitialized.complete(Unit)
                }
            }
        } else {
            _useLocalServer.value = false
            if (!isInitialized.isCompleted) isInitialized.complete(Unit)
        }
    }
}
