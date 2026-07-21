// Bless be the name of LORD GOD of hosts
package com.den.craftaday.ui.screens.welcomeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.den.craftaday.ui.theme.CraftADayTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.WelcomeViewModel
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.HORIZONTAL
import com.den.craftaday.ui.screens.screenManager.HomeRouter
import com.den.craftaday.ui.screens.screenManager.LoginRouter
import com.den.craftaday.ui.screens.screenManager.EmailRouter
import com.den.craftaday.ui.screens.screenManager.NameRouter
import com.den.craftaday.ui.screens.screenManager.WelcomeRouter

@Composable
fun WelcomeScreen(
    backStack: NavBackStack<NavKey>,
    viewModel: WelcomeViewModel
) {
    // User state management
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Navigation logic handled in LaunchedEffect to avoid side effects during composition
    LaunchedEffect(userState) {
        if (userState is AuthState.Authenticated) {
            val userId = (userState as AuthState.Authenticated).userId
            backStack.clear()
            backStack.add(HomeRouter(userId))
        }
    }

    WelcomeScreenContent(
        userState = userState,
        onLoginClick = { backStack.add(LoginRouter) },
        onRegisterClick = { backStack.add(NameRouter) },
        onGoogleClick = {
            viewModel.updateAuthState(AuthState.NotAuthenticated)
            viewModel.googleAuthorization(context)
        },
        onAnonymousClick = {
            viewModel.updateAuthState(AuthState.NotAuthenticated)
            viewModel.createAnonymousAccount()
        }
    )
}

@Composable
fun WelcomeScreenContent(
    userState: AuthState,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAnonymousClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HORIZONTAL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IntroContent(
                userState = userState
            )
            AuthButtons(
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick,
                onGoogleClick = onGoogleClick,
                onAnonymousClick = onAnonymousClick
            )
        }
    }
}


@Composable
fun IntroContent(userState: AuthState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = com.den.craftaday.R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        if (userState is AuthState.Error) {
            Text(
                text = userState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AuthButtons(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAnonymousClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AUTH_BUTTON_VERTICAL_SPACE)
    ) {
        LoginButton {
            onLoginClick()
        }

        RegisterButton {
            onRegisterClick()
        }

        GoogleButton {
            onGoogleClick()
        }

        AnonymousButton {
            onAnonymousClick()
        }
    }
}

@Preview(uiMode = 1)
@Composable
fun WelcomeScreenPreview() {
    CraftADayTheme {
        WelcomeScreenContent(
            userState = AuthState.NotAuthenticated,
            onLoginClick = {},
            onRegisterClick = {},
            onGoogleClick = {},
            onAnonymousClick = {}
        )
    }
}
