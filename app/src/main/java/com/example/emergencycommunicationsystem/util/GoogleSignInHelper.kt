package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.emergencycommunicationsystem.config.GoogleAuthConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
        if (data == null) return null

        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            val message = when (statusCode) {
                CommonStatusCodes.SIGN_IN_REQUIRED -> "Sign-in required. Please try again."
                CommonStatusCodes.INVALID_ACCOUNT -> "Invalid account. Please use a different Google account."
                CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check your connection."
                CommonStatusCodes.INTERNAL_ERROR -> "Internal Google Play Services error."
                CommonStatusCodes.DEVELOPER_ERROR -> "Configuration error (Developer error $statusCode). Check SHA-1 and Client ID."
                10 -> "Developer error (10): Google Console is not set up correctly. Check SHA-1 and package name."
                12500 -> "Sign-in failed. Please check Google Play Services."
                12501 -> "Sign-in cancelled by user."
                12502 -> "Sign-in in progress. Please wait."
                else -> "Google Sign-In failed (Status: $statusCode). ${e.localizedMessage}"
            }
            Log.e(TAG, "Google Sign-In Error: Status $statusCode, Message: $message", e)
            throw Exception(message)
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
