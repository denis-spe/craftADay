// The LORD, he is the GOD, the LORD, he is the GOD
package com.den.craftaday.ui.screens.loginScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.LoginViewModel
import com.den.craftaday.helper.validatorEmail
import com.den.craftaday.helper.validatorPassword
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH
import com.den.craftaday.ui.screens.AUTH_TEXT_FIELD_WIDTH
import com.den.craftaday.ui.screens.components.AuthButton
import com.den.craftaday.ui.screens.components.BackArrow
import com.den.craftaday.ui.screens.components.EmailTextField
import com.den.craftaday.ui.screens.components.FooterContent
import com.den.craftaday.ui.screens.components.PasswordTextField
import com.den.craftaday.ui.screens.components.PasswordWithConfirmationTextField
import com.den.craftaday.ui.screens.screenManager.HomeRouter
import com.den.craftaday.ui.screens.screenManager.PasswordRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(backStack: NavBackStack<NavKey>) {
    // Text fields states
    val emailState = rememberTextFieldState()
    val emailMessage = remember { mutableStateOf("") }
    val passwordState = rememberTextFieldState()
    val passwordMessage = remember { mutableStateOf("") }

    // Initialize the ViewModel
    val loginViewModel = hiltViewModel<LoginViewModel>()


    LaunchedEffect(emailState.text, passwordState.text) {
        emailMessage.value = ""
        passwordMessage.value = ""
    }

    // User state management
    val userState by loginViewModel.userState.collectAsStateWithLifecycle()

    // Loading state
    val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()

    // Navigation logic handled in LaunchedEffect to avoid side effects during composition
    LaunchedEffect(userState) {
        if (userState is AuthState.Authenticated) {
            val userId = (userState as AuthState.Authenticated).userId
            backStack.clear()
            backStack.add(HomeRouter)
        }
    }

    // Server error message
    val serverErrorMessage = if (userState is AuthState.Error) {
        (userState as AuthState.Error).message
    } else {
        null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    BackArrow(backStack = backStack)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    // Intro text
                    IntroLoginContent(serverErrorMessage = serverErrorMessage)

                    // Text fields and button interactive content
                    InteractiveLoginContent(
                        emailState = emailState,
                        emailMessage = emailMessage,
                        passwordState = passwordState,
                        passwordMessage = passwordMessage
                    ) {
                        if (
                            validatorEmail(emailState.text, emailMessage) &&
                            validatorPassword(
                                passwordState.text,
                                passwordMessage = passwordMessage
                            )
                        ) {

                            // 1. Clear the user state
                            loginViewModel.updateAuthState(AuthState.NotAuthenticated)

                            // 2. Login the user
                            loginViewModel.login(
                                emailState.text.toString(),
                                passwordState.text.toString()
                            )
                        }
                    }

                    // Footer text
                    FooterContent()
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.background
                )
            }
        }
    }
}


@Composable
fun IntroLoginContent(serverErrorMessage: String? = null) {
    val paragraph = serverErrorMessage ?: "Welcome back, please login to your craftADay account"
    val color = if (serverErrorMessage != null) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Login to craftADay",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = paragraph,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
fun InteractiveLoginContent(
    emailState: TextFieldState,
    emailMessage: MutableState<String>,
    passwordState: TextFieldState,
    passwordMessage: MutableState<String>,
    onNextClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AUTH_BUTTON_VERTICAL_SPACE)
    ) {
        EmailTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = emailState,
            label = "Email",
            placeholder = "Email",
            isError = emailMessage.value.isNotEmpty(),
            errorMessage = emailMessage.value,
            onNextClick = onNextClick
        )

        PasswordTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = passwordState,
            label = "Password",
            placeholder = "Password",
            isError = passwordMessage.value.isNotEmpty(),
            errorMessage = passwordMessage.value
        )

        AuthButton(
            modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH),
            text = "Login",
            onClick = onNextClick
        )
    }
}