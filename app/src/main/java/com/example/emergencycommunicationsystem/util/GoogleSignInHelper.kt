package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.emergencycommunicationsystem.config.GoogleAuthConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task

/**
 * Helper class for Google Sign-In functionality
 */
object GoogleSignInHelper {

    private const val TAG = "GoogleSignInHelper"

    /**
     * Create GoogleSignInClient with the configured client ID
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GoogleAuthConfig.CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign-in intent
     */
    fun getSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    /**
     * Handle the sign-in result from the activity
     * Returns the GoogleSignInAccount if successful, throws an exception with a descriptive message otherwise
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        if (data == null) {
            Log.e(TAG, "Sign-in result data is null")
            return null
        }

        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            val errorString = GoogleSignInStatusCodes.getStatusCodeString(statusCode)
            
            val message = when (statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> "Sign-in required. Please try again."
                CommonStatusCodes.INVALID_ACCOUNT -> "Invalid account. Please use a different Google account."
                CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check your internet connection."
                CommonStatusCodes.INTERNAL_ERROR -> "Internal Google Play Services error. Try clearing cache of Google Play Services."
                CommonStatusCodes.DEVELOPER_ERROR -> "DEVELOPER_ERROR: Check if SHA-1 is registered in Firebase/Google Console. Ensure CLIENT_ID matches."
                10 -> "DEVELOPER_ERROR (10): Likely SHA-1 fingerprint mismatch or incorrect package name in Google Console."
                16 -> "CANCELLED (16): The user cancelled the sign-in flow."
                12500 -> "SIGN_IN_FAILED (12500): Check if Google Play Services is up to date."
                12501 -> "USER_CANCELLED (12501): User clicked back or closed the picker."
                12502 -> "SIGN_IN_IN_PROGRESS (12502): Please wait for existing sign-in to complete."
                else -> "Google Sign-In failed (Status: $statusCode / $errorString). ${e.localizedMessage}"
            }

            // High-visibility logs for debugging
            Log.e(TAG, "====================================================")
            Log.e(TAG, "🚨 GOOGLE SIGN-IN ERROR")
            Log.e(TAG, "Status Code: $statusCode ($errorString)")
            Log.e(TAG, "Diagnosis: $message")
            Log.e(TAG, "Check: 1. local.properties has correct GOOGLE_WEB_CLIENT_ID")
            Log.e(TAG, "Check: 2. SHA-1 of THIS build matches Google Cloud Console")
            Log.e(TAG, "Check: 3. Package name matches exactly: com.example.emergencycommunicationsystem")
            Log.e(TAG, "====================================================")
            
            throw Exception(message)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In parsing", e)
            throw e
        }
    }

    /**
     * Sign out from Google
     */
    fun signOut(context: Context, onComplete: () -> Unit) {
        getGoogleSignInClient(context).signOut()
            .addOnCompleteListener { onComplete() }
    }

    /**
     * Get the last signed-in account (if any)
     */
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
