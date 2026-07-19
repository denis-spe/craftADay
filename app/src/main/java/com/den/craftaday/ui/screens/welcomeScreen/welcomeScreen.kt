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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.WelcomeViewModel
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.HORIZONTAL
import com.den.craftaday.ui.screens.screenManager.HomeRouter
import com.den.craftaday.ui.screens.screenManager.LoginRouter
import com.den.craftaday.ui.screens.screenManager.RegisterRouter
import com.den.craftaday.ui.screens.screenManager.WelcomeRouter

@Composable
fun WelcomeScreen(backStack: NavBackStack<NavKey>) {

    // ViewModel injection
    val viewModel: WelcomeViewModel = hiltViewModel()

    // User state management
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    when(val user = userState) {
        is AuthState.Authenticated -> {
            val userId = user.userId
            backStack.clear()
            backStack.add(HomeRouter(userId))
        }

        else -> {
            backStack.clear()
            backStack.add(WelcomeRouter)
        }
    }

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
                viewModel = viewModel,
                backStack = backStack
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
    viewModel: WelcomeViewModel,
    backStack: NavBackStack<NavKey>
){
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AUTH_BUTTON_VERTICAL_SPACE)
    ) {
        LoginButton {
            backStack.add(LoginRouter)
        }

        RegisterButton {
            backStack.add(RegisterRouter)
        }

        GoogleButton {
            viewModel.updateAuthState(AuthState.NotAuthenticated)
            viewModel.googleAuthorization(context)
        }

        AnonymousButton {
            viewModel.updateAuthState(AuthState.NotAuthenticated)
            viewModel.createAnonymousAccount()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(backStack = NavBackStack())
}