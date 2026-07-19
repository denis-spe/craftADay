// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.screenManager

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.ScreenManagerViewModel
import com.den.craftaday.ui.screens.homeScreen.HomeScreen
import com.den.craftaday.ui.screens.loadingScreen.LoadingScreen
import com.den.craftaday.ui.screens.loginScreen.LoginScreen
import com.den.craftaday.ui.screens.registerScreen.RegisterScreen
import com.den.craftaday.ui.screens.settingsScreen.SettingsScreen
import com.den.craftaday.ui.screens.welcomeScreen.WelcomeScreen

fun EntryProviderScope<NavKey>.featureAEntryBuilder(backStack: NavBackStack<NavKey>) {
    // ===== Welcome Screen =====
    entry<WelcomeRouter>(
        metadata = metadata {
//            put(NavDisplay.TransitionKey) {
//                // Slide new content up, keeping the old content in place underneath
//                slideInVertically(
//                    initialOffsetY = { it },
//                    animationSpec = tween(1000)
//                ) togetherWith ExitTransition.KeepUntilTransitionsFinished
//            }
            put(NavDisplay.PopTransitionKey) {
                // Slide old content down, revealing the new content in place underneath
                EnterTransition.None togetherWith
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(1000)
                        )
            }
            put(NavDisplay.PredictivePopTransitionKey) {
                // Slide old content down, revealing the new content in place underneath
                EnterTransition.None togetherWith
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(1000)
                        )
            }
        }
    ) {
        WelcomeScreen(backStack = backStack)
    }

    // ===== Register Screen =====
    entry<RegisterRouter> {
        RegisterScreen(backStack = backStack)
    }

    // ===== Login Screen =====
    entry<LoginRouter> {
        LoginScreen(backStack = backStack)
    }

    // ===== Home Screen =====
    entry<HomeRouter> {
        HomeScreen(it.userId, backStack = backStack)
    }

    // ===== Settings Screen =====
    entry<SettingsRouter> {
        SettingsScreen(backStack = backStack)
    }
}

@Composable
fun ScreenManager() {
    // 1. Instantiate the ScreenManagerViewModel
    val viewModel: ScreenManagerViewModel = hiltViewModel()

    val userState by viewModel.userState.collectAsStateWithLifecycle()

    if (userState is AuthState.Loading) {
        LoadingScreen()
        return
    }

    // 3. Check if the user is logged in or not
    val isLogIn = remember(userState) {
        when(val user = userState) {
            AuthState.NotAuthenticated -> WelcomeRouter
            is AuthState.Authenticated -> HomeRouter(user.userId)
            is AuthState.Error -> WelcomeRouter
            else -> WelcomeRouter // Should not happen due to early return
        }
    }

    // 4. Create the NavBackStack with the initial screen
    val backStack = rememberNavBackStack(isLogIn)


    // 5. Create the NavDisplay
    NavDisplay(
        entryDecorators = listOf(
            // Add the default decorators for managing scenes and saving state
            rememberSaveableStateHolderNavEntryDecorator(),
            // Then add the view model store decorator
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            featureAEntryBuilder(backStack = backStack)
        },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(500)
            )
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        },
    )
}


