package com.example.emergencycommunicationsystem.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.emergencycommunicationsystem.config.GoogleAuthConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Helper class for Google Sign-In functionality
 */
object GoogleSignInHelper {
    
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
     * Returns the GoogleSignInAccount if successful, null otherwise
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        if (data == null) return null
        
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            val account = task.getResult(ApiException::class.java)
            account
        } catch (e: ApiException) {
            // Sign-in failed
            null
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
