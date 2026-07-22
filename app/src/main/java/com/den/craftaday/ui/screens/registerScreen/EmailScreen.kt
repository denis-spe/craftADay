// The mightly GOD is my strength, he revealed to us through his son JESUS CHRIST
package com.den.craftaday.ui.screens.registerScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.viewModels.RegisterViewModel
import com.den.craftaday.helper.validatorEmail
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH
import com.den.craftaday.ui.screens.AUTH_TEXT_FIELD_WIDTH
import com.den.craftaday.ui.screens.components.AuthButton
import com.den.craftaday.ui.screens.components.BackArrow
import com.den.craftaday.ui.screens.components.EmailTextField
import com.den.craftaday.ui.screens.components.FooterContent
import com.den.craftaday.ui.screens.screenManager.PasswordRouter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EmailScreen(backStack: NavBackStack<NavKey>, registerViewModel: RegisterViewModel) {

    val emailState = rememberTextFieldState()
    val emailMessage = remember { mutableStateOf("") }


    LaunchedEffect(emailState.text) {
        emailMessage.value = ""
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                // Intro text
                IntroEmailContent()

                // Text fields and button interactive content
                InteractiveEmailContent(
                    emailState = emailState,
                    emailMessage = emailMessage
                ) {
                    if (validatorEmail(emailState.text, emailMessage)) {
                        registerViewModel.updateEmail(emailState.text.toString())
                        backStack.add(PasswordRouter)
                    }
                }

                // Footer text
                FooterContent()
            }
        }
    }
}


@Composable
fun IntroEmailContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Insert Your Email Address",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please enter your email address",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun InteractiveEmailContent(
    emailState: TextFieldState,
    emailMessage: MutableState<String>,
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

        AuthButton(
            modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH),
            text = "Next",
            onClick = onNextClick
        )
    }
}