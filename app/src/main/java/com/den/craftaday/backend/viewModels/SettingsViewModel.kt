// Glory be to the name of LORD GOD
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase
) : ViewModel() {
    val userState: StateFlow<AuthState> = authorizationUseCase.userState

    fun logout() {
        viewModelScope.launch {
            authorizationUseCase.signOutUseCase()
        }
    }
}
