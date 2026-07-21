// The LORD is my strength, he revealed to us through his son JESUS CHRIST
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.UserInfo
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authorizationUseCase: AuthorizationUseCase
) : ViewModel() {
    private val _userInfo = MutableStateFlow(UserInfo())
    val userState: StateFlow<AuthState> = authorizationUseCase.userState

    /**
     * Register user to the Firebase Authentication service.
     */
    fun registerUser() {
        // Get the user information from the UI
        val firstName = _userInfo.value.firstName
        val lastName = _userInfo.value.lastName
        val password = _userInfo.value.password
        val email = _userInfo.value.email

        // Register the user using the use case
        viewModelScope.launch {
            authorizationUseCase.registerUser(
                firstName = firstName,
                lastName = lastName,
                password = password,
                email = email
            )
        }
    }

    /**
     * Update the usernames' information.
     */
    fun updateUserName(firstName: String, lastName: String) {
        _userInfo.value = _userInfo.value.copy(
            firstName = firstName,
            lastName = lastName
        )
    }

    /**
     * Update the user's password.
     */
    fun updatePassword(password: String) {
        _userInfo.value = _userInfo.value.copy(
            password = password
        )
    }

    /**
     * Update the user's email.
     */
    fun updateEmail(email: String) {
        _userInfo.value = _userInfo.value.copy(
            email = email
        )
    }

    /**
     * Update the user's authentication state.
     */
    fun updateAuthState(authState: AuthState){
        authorizationUseCase.updateAuthState(authState)
    }
}