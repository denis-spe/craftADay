// The LORD GOD of heaven and earth, The LORD of hosts.
package com.den.craftaday.backend.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.impl.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase,
): ViewModel() {
    val userState: StateFlow<AuthState> = authorizationUseCase.userState

    fun createAnonymousAccount() {
        viewModelScope.launch {
            authorizationUseCase.createAnonymousAccountUseCase()
        }
    }

    fun googleAuthorization(context: Context){
        viewModelScope.launch {
            authorizationUseCase.googleAuthUseCase(context)
        }
    }

    fun updateAuthState(authState: AuthState){
        authorizationUseCase.updateAuthState(authState)
    }
}