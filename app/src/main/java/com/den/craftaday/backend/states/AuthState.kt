package com.den.craftaday.backend.states

sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(
        val userId: String,
        val displayName: String? = null,
        val email: String? = null,
        val photoUrl: String? = null,
        val isAnonymous: Boolean
    ) : AuthState()

    data class Error(val message: String) : AuthState()
}