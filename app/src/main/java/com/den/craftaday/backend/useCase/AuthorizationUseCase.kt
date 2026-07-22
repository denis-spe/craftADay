// Bless be the name of LORD GOD of hosts
package com.den.craftaday.backend.useCase

import android.content.Context
import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.states.AuthState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthorizationUseCase @Inject constructor(
    private val accountService: AccountService
) {
    val userState: StateFlow<AuthState> = accountService.userState

    suspend fun googleAuthUseCase(
        context: Context,
    ) {
        accountService.handleGoogleSignIn(context)
    }

    suspend fun signOutUseCase() {
        accountService.signOut()
    }

    suspend fun createAnonymousAccountUseCase() {
        accountService.createAnonymousAccount()
    }

    fun updateAuthState(authState: AuthState){
        accountService.updateState(authState)
    }

    suspend fun registerUser(
        firstName: String,
        lastName: String,
        password: String,
        email: String,
    ) {
        accountService.register(
            firstName,
            lastName = lastName,
            password = password,
            email = email
        )
    }

    suspend fun login(
        email: String,
        password: String,
    ) {
        accountService.login(
            email = email,
            password = password
        )
    }
}

