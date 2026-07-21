package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import com.den.craftaday.backend.impl.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ScreenManagerViewModel @Inject constructor(
    authorizationUseCase: AuthorizationUseCase
) : ViewModel() {
    val userState: StateFlow<AuthState> = authorizationUseCase.userState
}