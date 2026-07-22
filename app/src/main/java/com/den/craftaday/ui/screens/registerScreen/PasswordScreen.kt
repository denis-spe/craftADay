// The LORD is my rock and fortress
package com.den.craftaday.ui.screens.registerScreen

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.viewModels.RegisterViewModel
import com.den.craftaday.helper.validatorPasswordWithConfirmation
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH
import com.den.craftaday.ui.screens.AUTH_TEXT_FIELD_WIDTH
import com.den.craftaday.ui.screens.components.AuthButton
import com.den.craftaday.ui.screens.components.BackArrow
import com.den.craftaday.ui.screens.components.FooterContent
import com.den.craftaday.ui.screens.components.PasswordWithConfirmationTextField
import com.den.craftaday.ui.screens.screenManager.HomeRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordScreen(backStack: NavBackStack<NavKey>, registerViewModel: RegisterViewModel) {

    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    val passwordMessage = remember { mutableStateOf("") }
    val confirmPasswordMessage = remember { mutableStateOf("") }


    LaunchedEffect(passwordState.text, confirmPasswordState.text) {
        passwordMessage.value = ""
        confirmPasswordMessage.value = ""
    }

    // User state management
    val userState by registerViewModel.userState.collectAsStateWithLifecycle()

    // Loading state
    val isLoading by registerViewModel.isLoading.collectAsStateWithLifecycle()

    // Navigation logic handled in LaunchedEffect to avoid side effects during composition
    LaunchedEffect(userState) {
        if (userState is AuthState.Authenticated) {
            val userId = (userState as AuthState.Authenticated).userId
            backStack.clear()
            backStack.add(HomeRouter)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    BackArrow(backStack = backStack) {
                        registerViewModel.updateAuthState(AuthState.NotAuthenticated)
                    }
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(25.dp)
                ) {
                    // Intro text
                    IntroPasswordContent(
                        serverErrorMessage = if (userState is AuthState.Error) {
                            (userState as AuthState.Error).message
                        } else {
                            null
                        }
                    )

                    // Text fields and button interactive content
                    InteractivePasswordContent(
                        passwordState = passwordState,
                        confirmPasswordState = confirmPasswordState,
                        passwordMessage = passwordMessage,
                        confirmPasswordMessage = confirmPasswordMessage
                    ) {
                        if (validatorPasswordWithConfirmation(
                                passwordState = passwordState.text,
                                confirmPasswordState = confirmPasswordState.text,
                                passwordMessage = passwordMessage,
                                confirmPasswordMessage = confirmPasswordMessage
                            )
                        ) {
                            // 1. Clear the user state
                            registerViewModel.updateAuthState(AuthState.NotAuthenticated)

                            // 2. Update the user password information
                            registerViewModel.updatePassword(passwordState.text.toString())

                            // 3. Register the user
                            registerViewModel.registerUser()
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
fun IntroPasswordContent(serverErrorMessage: String?) {

    val paragraph = serverErrorMessage ?: "Add a secure password to protect your account"
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
            text = "Fill In Your Password",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = paragraph,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InteractivePasswordContent(
    passwordState: TextFieldState,
    confirmPasswordState: TextFieldState,
    passwordMessage: MutableState<String>,
    confirmPasswordMessage: MutableState<String>,
    onNextClick: () -> Unit
) {

    val arePasswordsMatching = passwordState.text == confirmPasswordState.text

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AUTH_BUTTON_VERTICAL_SPACE)
    ) {
        PasswordWithConfirmationTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = passwordState,
            label = "Password",
            placeholder = "Password",
            isError = passwordMessage.value.isNotEmpty(),
            errorMessage = passwordMessage.value,
        )

        PasswordWithConfirmationTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = confirmPasswordState,
            label = "Confirm Password",
            placeholder = "Confirm Password",
            isError = confirmPasswordMessage.value.isNotEmpty() && !arePasswordsMatching,
            errorMessage = confirmPasswordMessage.value,
        )

        AuthButton(
            modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH),
            text = "Register",
            onClick = onNextClick
        )
    }
}