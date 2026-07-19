package com.den.craftaday.backend.impl

import android.content.Context
import androidx.credentials.GetCredentialRequest
import com.den.craftaday.backend.states.AuthState
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AccountService {

    /**
     * Firebase Auth instance
     */
    val auth: FirebaseAuth

    /**
     * StateFlow of the current user
     */
    val userState: StateFlow<AuthState>

    /**
     * Current user ID
     */
    val currentUserId: String

    /**
     * Is user logged in
     */
    val hasUser: Boolean

    /**
     * Handles Google Sign In
     */
    suspend fun handleGoogleSignIn(
        context: Context,
    ): Exception?

    /**
     * Login with email and password
     * @param email User email
     * @param password User password
     */
    suspend fun login(email: String, password: String)

    /**
     * Register with email and password
     * @param firstName User first name
     * @param lastName User last name
     * @param email User email
     * @param password User password
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    )

    /**
     * Send recovery email
     * @param email User email
     */
    suspend fun sendRecoveryEmail(email: String)

    /**
     * Create anonymous account
     */
    suspend fun createAnonymousAccount()

    /**
     * Link account with email and password
     * @param email User email
     * @param password User password
     */
    suspend fun linkAccount(email: String, password: String)

    /**
     * Update state
     * @param authState AuthState
     */
    fun updateState(authState: AuthState)


    /**
     * Delete account
     */
    suspend fun deleteAccount()


    /**
     * Sign out
     */
    suspend fun signOut()
}