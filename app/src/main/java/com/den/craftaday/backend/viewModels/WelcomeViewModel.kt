// The LORD GOD of heaven and earth, The LORD of hosts.
package com.den.craftaday.backend.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase,
): ViewModel() {
    val userState: StateFlow<AuthState> = authorizationUseCase.userState
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun createAnonymousAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            authorizationUseCase.createAnonymousAccountUseCase()
            _isLoading.value = false
        }
    }

    fun googleAuthorization(context: Context){
        viewModelScope.launch {
            _isLoading.value = true
            authorizationUseCase.googleAuthUseCase(context)
            _isLoading.value = false
        }
    }

    fun updateAuthState(authState: AuthState){
        authorizationUseCase.updateAuthState(authState)
    }
}