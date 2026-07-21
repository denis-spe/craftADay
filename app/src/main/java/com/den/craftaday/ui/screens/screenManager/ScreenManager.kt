// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.screenManager

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.backend.viewModels.RegisterViewModel
import com.den.craftaday.backend.viewModels.ScreenManagerViewModel
import com.den.craftaday.backend.viewModels.SettingsViewModel
import com.den.craftaday.backend.viewModels.WelcomeViewModel
import com.den.craftaday.ui.screens.homeScreen.HomeScreen
import com.den.craftaday.ui.screens.loadingScreen.LoadingScreen
import com.den.craftaday.ui.screens.loginScreen.LoginScreen
import com.den.craftaday.ui.screens.registerScreen.EmailScreen
import com.den.craftaday.ui.screens.registerScreen.NameScreen
import com.den.craftaday.ui.screens.registerScreen.PasswordScreen
import com.den.craftaday.ui.screens.settingsScreen.SettingsScreen
import com.den.craftaday.ui.screens.welcomeScreen.WelcomeScreen

fun EntryProviderScope<NavKey>.featureAEntryBuilder(
    backStack: NavBackStack<NavKey>,
    registerViewModel: RegisterViewModel,
    welcomeViewModel: WelcomeViewModel,
    settingsViewModel: SettingsViewModel,
    homeViewModel: HomeViewModel,
) {
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
        WelcomeScreen(
            backStack = backStack,
            viewModel = welcomeViewModel
        )
    }

    // ===== Email Screen =====
    entry<EmailRouter> {
        EmailScreen(
            backStack = backStack,
            registerViewModel = registerViewModel
        )
    }

    // ===== Name Screen =====
    entry<NameRouter> {
        NameScreen(
            backStack = backStack,
            registerViewModel = registerViewModel
        )
    }

    // ===== Password Screen =====
    entry<PasswordRouter> {
        PasswordScreen(
            backStack = backStack,
            registerViewModel = registerViewModel
        )
    }

    // ===== Login Screen =====
    entry<LoginRouter> {
        LoginScreen(backStack = backStack)
    }

    // ===== Home Screen =====
    entry<HomeRouter> {
        HomeScreen(
            it.userId,
            backStack = backStack,
            homeViewModel = homeViewModel
        )
    }

    // ===== Settings Screen =====
    entry<SettingsRouter> {
        SettingsScreen(
            backStack = backStack,
            settingsViewModel = settingsViewModel
        )
    }
}

@Composable
fun ScreenManager() {
    // 1. Instantiate the viewModel
    val viewModel: ScreenManagerViewModel = hiltViewModel()
    val registerViewModel: RegisterViewModel = hiltViewModel()
    val welcomeViewModel: WelcomeViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    // 2. Observe the userState
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
            featureAEntryBuilder(
                backStack = backStack,
                registerViewModel = registerViewModel,
                welcomeViewModel = welcomeViewModel,
                settingsViewModel = settingsViewModel,
                homeViewModel = homeViewModel
            )
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


