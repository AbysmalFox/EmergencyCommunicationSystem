package com.example.emergencycommunicationsystem.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.emergencycommunicationsystem.BuildConfig
import com.example.emergencycommunicationsystem.network.AlertsApiService
import com.example.emergencycommunicationsystem.network.AuthApiService
import com.example.emergencycommunicationsystem.network.CallApiService
import com.example.emergencycommunicationsystem.network.IncidentApiService
import com.example.emergencycommunicationsystem.network.MessagingApiService
import com.example.emergencycommunicationsystem.network.SettingsApiService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

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
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
        
        context?.let { ctx ->
            val cacheSize = 10 * 1024 * 1024L // 10 MB
            val cacheDir = File(ctx.cacheDir, "http_cache")
            val cache = Cache(cacheDir, cacheSize)
            builder.cache(cache)
            
            builder.addInterceptor { chain ->
                var request = chain.request()
                if (!isNetworkAvailable(ctx)) {
                    request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                        .build()
                }
                chain.proceed(request)
            }
        }
        return builder.build()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun getRetrofit(isLocal: Boolean): Retrofit {
        return if (isLocal) {
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
        return getRetrofit(_useLocalServer.value).create(AuthApiService::class.java)
    }

    suspend fun alertsApiService(): AlertsApiService {
        isInitialized.await()
        return getRetrofit(_useLocalServer.value).create(AlertsApiService::class.java)
    }

    suspend fun messagingApiService(): MessagingApiService {
        isInitialized.await()
        return getRetrofit(_useLocalServer.value).create(MessagingApiService::class.java)
    }

    suspend fun settingsApiService(): SettingsApiService {
        isInitialized.await()
        return getRetrofit(_useLocalServer.value).create(SettingsApiService::class.java)
    }

    suspend fun incidentApiService(): IncidentApiService {
        isInitialized.await()
        return getRetrofit(_useLocalServer.value).create(IncidentApiService::class.java)
    }

    suspend fun callApiService(): CallApiService {
        isInitialized.await()
        return getRetrofit(_useLocalServer.value).create(CallApiService::class.java)
    }

    fun initializeAndCheckConnection(context: Context) {
        if (isInitialized.isCompleted) return

        // Never allow initialization failures to block all API calls forever.
        runCatching {
            // Pre-initialize client with context (enables cache/interceptors)
            getOkHttpClient(context.applicationContext)
        }.onFailure { e ->
            Log.e("ApiClient", "OkHttp pre-initialization failed; continuing without context-backed cache", e)
        }

        if (BuildConfig.ALLOW_LOCAL_FALLBACK) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = Request.Builder()
                        .url(NetworkConfig.PRODUCTION_API_URL + "alerts.php")
                        .head()
                        .build()
                    
                    // Crucial: Use withContext(Dispatchers.IO) for the blocking OkHttp call
                    val response = withContext(Dispatchers.IO) {
                        getOkHttpClient().newCall(request).execute()
                    }
                    
                    if (response.isSuccessful) {
                        _useLocalServer.value = false
                        Log.i("ApiClient", "Production server reachable.")
                    } else {
                        _useLocalServer.value = true
                        Log.w("ApiClient", "Production server returned ${response.code}. Falling back.")
                    }
                } catch (e: Exception) {
                    _useLocalServer.value = true
                    Log.e("ApiClient", "Check failed", e)
                } finally {
                    if (!isInitialized.isCompleted) {
                        isInitialized.complete(Unit)
                    }
                }
            }
        } else {
            try {
                _useLocalServer.value = false
                Log.i("ApiClient", "Local fallback disabled. Using production backend only.")
            } finally {
                if (!isInitialized.isCompleted) {
                    isInitialized.complete(Unit)
                }
            }
        }
    }
}
