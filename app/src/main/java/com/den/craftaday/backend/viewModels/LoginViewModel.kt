// Bless be the name of LORD GOD of hosts
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase
) : ViewModel() {
    val userState = authorizationUseCase.userState
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            authorizationUseCase.login(
                email = email,
                password = password
            )
            _isLoading.value = false
        }
    }

    fun updateAuthState(
        authState: AuthState
    ){
        authorizationUseCase.updateAuthState(authState)
    }
}