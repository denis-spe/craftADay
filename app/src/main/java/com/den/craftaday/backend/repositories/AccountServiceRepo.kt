// Glory be to name of LORD GOD of host
package com.den.craftaday.backend.repositories

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.util.Log
import com.den.craftaday.R
import androidx.annotation.RequiresApi
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.den.craftaday.backend.impl.AccountService
import com.den.craftaday.backend.states.AuthState
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Base64

class AccountServiceRepo(
    override val auth: FirebaseAuth,
) : AccountService {

    private val _userState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val userState: StateFlow<AuthState> = _userState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _userState.value = AuthState.Authenticated(
                    userId = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    isAnonymous = user.isAnonymous
                )
            } else {
                _userState.value = AuthState.NotAuthenticated
            }
        }
    }

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = auth.currentUser != null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun signIn(
        request: GetCredentialRequest,
        context: Context,
    ): Exception? {
        val credentialManager = CredentialManager.create(context)

        val result = try {
            credentialManager.getCredential(request = request, context = context)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            if (e is GetCredentialCancellationException) {
                _userState.value = AuthState.Error("Sign in cancelled")
            }
            return e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            return e
        }

        val credential: Credential = result.credential
        Log.i(TAG, "CredentialManager returned: ${credential::class.java.simpleName}")

        when (credential) {
            is PublicKeyCredential -> {
                // Passkey flow: send the authenticationResponseJson to your backend to verify
                credential.authenticationResponseJson
                Log.i(TAG, "Received PublicKeyCredential — send to server for verification")
                // TODO: send responseJson to your backend for verification (passkeys)
                return null
            }

            is PasswordCredential -> {
                // Username/password saved credential — sign in with Email/Password provider
                val email = credential.id
                val password = credential.password
                Log.i(TAG, "Received PasswordCredential for id=$email")
                auth.signInWithEmailAndPassword(email, password).await()
                _userState.value = AuthState.Authenticated(
                    userId = auth.currentUser!!.uid,
                    displayName = auth.currentUser!!.displayName ?: "",
                    email = auth.currentUser!!.email ?: "",
                    photoUrl = auth.currentUser!!.photoUrl?.toString(),
                    isAnonymous = auth.currentUser!!.isAnonymous
                )
                return null
            }

            is CustomCredential -> {
                // Google ID token results are returned as a CustomCredential with a specific type
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken // getIdToken() equivalent
                        if (idToken.isNotEmpty()) {
                            val firebaseCred: AuthCredential =
                                GoogleAuthProvider.getCredential(idToken, null)
                            val credential = auth.signInWithCredential(firebaseCred)
                            val firebaseUser = credential.await().user
                            _userState.value = AuthState.Authenticated(
                                userId = firebaseUser!!.uid,
                                displayName = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString(),
                                isAnonymous = firebaseUser.isAnonymous
                            )
                            Log.i(TAG, "Signed into Firebase via Google: uid=${firebaseUser?.uid}")
                            return null
                        } else {
                            _userState.value = AuthState.Error("Google ID token is empty")
                            Log.e(TAG, "GoogleIdTokenCredential has empty id token")
                            return IllegalStateException("Received empty Google ID token")
                        }
                    } catch (e: NoCredentialException) {
                        _userState.value = AuthState.Error("Failed to get credential from CustomCredential")
                        Log.e(TAG, "Failed to get credential from CustomCredential", e)
                        return e
                    }
                } else {
                    Log.w(TAG, "Unhandled CustomCredential type: ${credential.type}")
                    _userState.value = AuthState.Error("Unhandled CustomCredential type: ${credential.type}")
                    return IllegalArgumentException("Unhandled CustomCredential type: ${credential.type}")
                }
            }

            else -> {
                Log.w(TAG, "Unexpected credential type: ${credential::class.java}")
                _userState.value = AuthState.Error("Unexpected credential type: ${credential::class.java}")
                return IllegalArgumentException("Unsupported credential type: ${credential::class.java}")
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun handleGoogleSignIn(
        context: Context,
    ): Exception? {
        val webClientId: String = context.getString(R.string.default_web_client_id)

        // For an explicit button flow, we use GetSignInWithGoogleOption which
        // shows the account picker with all accounts immediately.
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = webClientId
        ).setNonce(generateSecureRandomNonce())
            .build()

        // Create a credential request with the Google ID option.
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        // Attempt to sign in with the created request
        return this.signIn(request, context)
    }

    override suspend fun login(email: String, password: String) {
        try {
            val user = auth.signInWithEmailAndPassword(email, password).await().user
            _userState.value = AuthState.Authenticated(
                userId = user!!.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
                .user?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName("$firstName $lastName").build()
                )
            _userState.value = AuthState.Authenticated(
                userId = auth.currentUser!!.uid,
                displayName = auth.currentUser!!.displayName ?: "",
                email = auth.currentUser!!.email ?: "",
                photoUrl = auth.currentUser!!.photoUrl?.toString(),
                isAnonymous = auth.currentUser!!.isAnonymous
            )
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun sendRecoveryEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun createAnonymousAccount() {
        try {
            val user = auth.signInAnonymously().await().user
            _userState.value = AuthState.Authenticated(
                userId = user!!.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun linkAccount(email: String, password: String) {
        try {
            val credential = EmailAuthProvider.getCredential(email, password)

            val user = auth.currentUser!!.linkWithCredential(credential).await().user
            _userState.value = AuthState.Authenticated(
                userId = user!!.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun deleteAccount() {
        try {
            auth.currentUser!!.delete().await()
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    override fun updateState(authState: AuthState) {
        _userState.value = authState
    }

    override suspend fun signOut() {
        try {
            if (auth.currentUser?.isAnonymous == true) {
                auth.currentUser?.delete()?.await()
                _userState.value = AuthState.NotAuthenticated
            }
            auth.signOut()
        } catch (e: Exception) {
            _userState.value = AuthState.Error(e.message ?: "Unknown error")
            Log.e("Auth", "Sign out error — forcing local sign out anyway", e)
            auth.signOut()  // still sign out locally
        } finally {
            _userState.value = AuthState.NotAuthenticated
        }
    }
}