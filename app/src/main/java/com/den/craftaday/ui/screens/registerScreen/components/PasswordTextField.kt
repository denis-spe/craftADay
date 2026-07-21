// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.registerScreen.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    textState: TextFieldState,
    label: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String,
    arePasswordsMatching: Boolean,
    onNextClick: () -> Unit = {},
) {
    val showPassword = remember { mutableStateOf(false) }

    OutlinedSecureTextField(
        modifier = modifier,
        state = textState,
        label = { Text(
            label
        ) },
        placeholder = { Text(
            placeholder,
            fontWeight = FontWeight.Medium
        ) },
        shape = RoundedCornerShape(50),
        isError = isError && !arePasswordsMatching,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Send
        ),
        supportingText = {
            if (isError) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        onKeyboardAction = KeyboardActionHandler {
            onNextClick()
        },
        inputTransformation = InputTransformation.maxLength(8),
        textObfuscationMode = if (showPassword.value) {
            TextObfuscationMode.Visible
        } else {
            TextObfuscationMode.RevealLastTyped
        },
        trailingIcon = {
            IconToggleButton(
                checked = showPassword.value,
                onCheckedChange = { showPassword.value = it }
            ) {
                Icon(
                    imageVector = if (showPassword.value) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (showPassword.value) {
                        "Hide password"
                    } else {
                        "Show password"
                    }
                )
            }
        }
    )
}
