package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.emergencycommunicationsystem.BuildConfig
import com.example.emergencycommunicationsystem.config.GoogleAuthConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import java.security.MessageDigest

/**
 * Helper class for Google Sign-In functionality.
 */
object GoogleSignInHelper {

    private const val TAG = "GoogleSignInHelper"

    private fun resolveWebClientId(context: Context): String {
        val localClientId = GoogleAuthConfig.WEB_CLIENT_ID.trim()
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        val firebaseClientId = if (resId != 0) context.getString(resId).trim() else ""

        if (firebaseClientId.isBlank()) {
            Log.w(
                TAG,
                "R.string.default_web_client_id is missing/blank. " +
                    "google-services.json likely has no oauth_client entries."
            )
        }

        if (firebaseClientId.isNotBlank() && localClientId.isNotBlank() && firebaseClientId != localClientId) {
            Log.w(
                TAG,
                "Client ID mismatch: local.properties differs from google-services.json; " +
                    "using local.properties for backend audience alignment."
            )
        }

        if (localClientId.isNotBlank()) {
            Log.i(TAG, "Using web client id from local.properties")
            return localClientId
        }

        Log.i(TAG, "Using web client id from google-services.json")
        return firebaseClientId
    }

    private fun ByteArray.toColonHex(): String = joinToString(":") { "%02X".format(it) }

    private fun logRuntimeSigningFingerprints(context: Context) {
        runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners?.map { it.toByteArray() }.orEmpty()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures?.map { it.toByteArray() }.orEmpty()
            }

            signatures.forEachIndexed { index, raw ->
                val sha1 = MessageDigest.getInstance("SHA-1").digest(raw).toColonHex()
                val sha256 = MessageDigest.getInstance("SHA-256").digest(raw).toColonHex()
                Log.i(TAG, "Runtime signing cert[$index] SHA-1: $sha1")
                Log.i(TAG, "Runtime signing cert[$index] SHA-256: $sha256")
            }
        }.onFailure {
            Log.w(TAG, "Unable to read runtime signing fingerprints", it)
        }
    }

    /**
     * Create GoogleSignInClient with the configured web client ID.
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val webClientId = resolveWebClientId(context)
        logRuntimeSigningFingerprints(context)
        require(webClientId.isNotBlank()) {
            "Google Web Client ID is empty. Configure Firebase Auth Google provider, " +
                "download updated google-services.json, or set GOOGLE_WEB_CLIENT_ID."
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign-in intent.
     */
    fun getSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    /**
     * Handle the sign-in result from the activity.
     * Returns the GoogleSignInAccount if successful, throws an exception with a descriptive message otherwise.
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
                CommonStatusCodes.DEVELOPER_ERROR -> "DEVELOPER_ERROR: Check SHA-1/SHA-256, package name, and Web Client ID."
                10 -> "DEVELOPER_ERROR (10): Likely SHA-1/SHA-256 mismatch or incorrect package name."
                16 -> "CANCELLED (16): The user cancelled the sign-in flow."
                12500 -> "SIGN_IN_FAILED (12500): Check if Google Play Services is up to date."
                12501 -> "USER_CANCELLED (12501): User clicked back or closed the picker."
                12502 -> "SIGN_IN_IN_PROGRESS (12502): Please wait for existing sign-in to complete."
                else -> "Google Sign-In failed (Status: $statusCode / $errorString). ${e.localizedMessage}"
            }

            Log.e(TAG, "====================================================")
            Log.e(TAG, "GOOGLE SIGN-IN ERROR")
            Log.e(TAG, "Status Code: $statusCode ($errorString)")
            Log.e(TAG, "Diagnosis: $message")
            Log.e(TAG, "Runtime package: ${BuildConfig.APPLICATION_ID}")
            Log.e(TAG, "Check: 1. Firebase Auth > Sign-in method > Google is enabled")
            Log.e(TAG, "Check: 2. google-services.json has oauth_client for this app")
            Log.e(TAG, "Check: 3. SHA-1 and SHA-256 of this keystore are registered in Firebase")
            Log.e(TAG, "Check: 4. GOOGLE_WEB_CLIENT_ID matches Firebase Web client ID")
            Log.e(TAG, "====================================================")

            throw Exception(message)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In parsing", e)
            throw e
        }
    }

    /**
     * Sign out from Google.
     */
    fun signOut(context: Context, onComplete: () -> Unit) {
        getGoogleSignInClient(context).signOut()
            .addOnCompleteListener { onComplete() }
    }

    /**
     * Get the last signed-in account (if any).
     */
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}
