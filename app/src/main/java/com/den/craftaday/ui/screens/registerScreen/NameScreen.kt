// The LORD is my strength, he revealed to us through his son JESUS CHRIST
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.viewModels.RegisterViewModel
import com.den.craftaday.helper.validateName
import com.den.craftaday.ui.screens.AUTH_BUTTON_VERTICAL_SPACE
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH
import com.den.craftaday.ui.screens.AUTH_TEXT_FIELD_WIDTH
import com.den.craftaday.ui.screens.registerScreen.components.AuthButton
import com.den.craftaday.ui.screens.registerScreen.components.BackArrow
import com.den.craftaday.ui.screens.registerScreen.components.FooterContent
import com.den.craftaday.ui.screens.registerScreen.components.NameTextField
import com.den.craftaday.ui.screens.screenManager.EmailRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameScreen(
    backStack: NavBackStack<NavKey>,
    registerViewModel: RegisterViewModel,
) {
    val firstNameState = rememberTextFieldState()
    val lastNameState = rememberTextFieldState()
    val firstNameMessage = remember { mutableStateOf("") }
    val lastNameMessage = remember { mutableStateOf("") }


    LaunchedEffect(firstNameState.text, lastNameState.text) {
        firstNameMessage.value = ""
        lastNameMessage.value = ""
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
            modifier = Modifier.fillMaxSize()
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
                IntroNameContent()

                // Text fields and button interactive content
                InteractiveNameContent(
                    firstNameState = firstNameState,
                    lastNameState = lastNameState,
                    firstNameMessage = firstNameMessage,
                    lastNameMessage = lastNameMessage
                ) {
                    if (validateName(
                            firstNameState = firstNameState.text,
                            lastNameState = lastNameState.text,
                            firstNameMessage = firstNameMessage,
                            lastNameMessage = lastNameMessage
                    )) {
                        registerViewModel.updateUserName(
                            firstNameState.text.toString(),
                            lastNameState.text.toString()
                        )
                        backStack.add(EmailRouter)
                    }
                }

                // Footer text
                FooterContent()
            }
        }
    }
}



@Composable
fun IntroNameContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Enter Your Names",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please enter your first and last name",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun InteractiveNameContent(
    firstNameState: TextFieldState,
    lastNameState: TextFieldState,
    firstNameMessage: MutableState<String>,
    lastNameMessage: MutableState<String>,
    onNextClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AUTH_BUTTON_VERTICAL_SPACE)
    ) {
        NameTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = firstNameState,
            label = "First Name",
            placeholder = "First Name",
            isError = firstNameMessage.value.isNotEmpty(),
            errorMessage = firstNameMessage.value,
            onNextClick = onNextClick
        )

        NameTextField(
            modifier = Modifier.fillMaxWidth(AUTH_TEXT_FIELD_WIDTH),
            textState = lastNameState,
            label = "Last Name",
            placeholder = "Optional",
            isError = lastNameMessage.value.isNotEmpty(),
            errorMessage = lastNameMessage.value,
            onNextClick = onNextClick,
        )

        AuthButton(
            modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH),
            text = "Next",
            onClick = onNextClick
        )
    }
}