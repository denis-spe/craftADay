// The LORD GOD is holy, the mightly GOD is holy
package com.den.craftaday.ui.screens.screenManager

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class HomeRouter(val userId: String): NavKey


@Serializable
data object WelcomeRouter: NavKey

@Serializable
data object LoginRouter: NavKey

@Serializable
data object RegisterRouter: NavKey

@Serializable
data object ForgotPasswordRouter: NavKey

@Serializable
data object SettingsRouter: NavKey
