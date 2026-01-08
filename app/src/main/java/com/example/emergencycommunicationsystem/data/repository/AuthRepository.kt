package com.example.emergencycommunicationsystem.data.repository

import android.content.Context
import com.example.emergencycommunicationsystem.data.AuthResponse
import com.example.emergencycommunicationsystem.data.LoginRequest
import com.example.emergencycommunicationsystem.data.LogoutRequest
import com.example.emergencycommunicationsystem.data.RegisterRequest
import com.example.emergencycommunicationsystem.data.network.ApiClient
import com.example.emergencycommunicationsystem.network.AuthApiService
import com.example.emergencycommunicationsystem.util.DeviceManager
import com.example.emergencycommunicationsystem.util.LocationUtils
import retrofit2.HttpException
import retrofit2.Response

class AuthRepository {

    private suspend fun apiService(): AuthApiService = ApiClient.authApiService()

    private suspend fun <T> executeApiCall(call: suspend () -> Response<T>): T {
        try {
            val response = call()
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Server returned an empty response body.")
            } else {
                throw HttpException(response)
            }
        } catch (e: Exception) {
            // Re-throw any exception to be handled by the ViewModel
            throw e
        }
    }

    suspend fun login(context: Context, request: LoginRequest): AuthResponse {
        val loginData = mutableMapOf<String, Any>()
        request.email?.let { loginData["email"] = it }
        request.phone?.let { loginData["phone"] = it }
        loginData["password"] = request.password

        loginData["device_id"] = DeviceManager.getDeviceId(context)
        loginData["device_type"] = "android"
        loginData["device_name"] = DeviceManager.getDeviceName()
        loginData["push_token"] = DeviceManager.getPushToken()

        return executeApiCall { apiService().loginUser(loginData) }
    }

    suspend fun register(context: Context, request: RegisterRequest): AuthResponse {
        val registerData = mutableMapOf<String, Any>()
        registerData["name"] = request.name
        registerData["email"] = request.email
        registerData["password"] = request.password
        registerData["phone"] = request.phone
        registerData["share_location"] = request.shareLocation

        registerData["device_id"] = DeviceManager.getDeviceId(context)
        registerData["device_type"] = "android"
        registerData["device_name"] = DeviceManager.getDeviceName()
        registerData["push_token"] = DeviceManager.getPushToken()

        // Add location data if available
        request.latitude?.let { registerData["latitude"] = it }
        request.longitude?.let { registerData["longitude"] = it }
        request.address?.let { registerData["address"] = it }

        return executeApiCall { apiService().registerUser(registerData) }
    }

    suspend fun logout(context: Context, userId: Int): AuthResponse {
        val deviceId = DeviceManager.getDeviceId(context)
        val request = LogoutRequest(userId = userId, deviceId = deviceId)
        return executeApiCall { apiService().logout(request) }
    }
}