package com.example.emergencycommunicationsystem.network

import com.example.emergencycommunicationsystem.data.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("register.php")
    suspend fun registerUser(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse>

    @POST("login.php")
    suspend fun loginUser(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse>

    @POST("user/profile_data.php")
    suspend fun getProfileData(@Body request: ProfileDataRequest): ProfileDataResponse

    @POST("logout.php")
    suspend fun logout(@Body request: LogoutRequest): Response<AuthResponse>

    @Multipart
    @POST("user/update_profile.php")
    suspend fun updateProfile(
        @Part("user_id") userId: RequestBody,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part profilePic: MultipartBody.Part?
    ): UpdateProfileResponse

    @POST("user/change_password.php")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ChangePasswordResponse>

    @POST("email_otp/request_email_otp.php")
    suspend fun requestEmailOtp(@Body request: RequestEmailOtpRequest): Response<EmailOtpResponse>

    @POST("email_otp/verify_email_otp.php")
    suspend fun verifyEmailOtp(@Body request: VerifyEmailOtpRequest): Response<EmailOtpResponse>
}
