// Bless be the name of LORD GOD of hosts
package com.den.craftaday.ui.screens.registerScreen.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    textState: TextFieldState,
    label: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String,
    onNextClick: () -> Unit
) {
    OutlinedTextField(
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
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
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
        }
    )
}