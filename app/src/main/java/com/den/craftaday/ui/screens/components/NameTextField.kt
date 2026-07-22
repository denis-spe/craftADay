// Bless be the name of LORD GOD of hosts
package com.den.craftaday.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.den.craftaday.ui.theme.CraftADayTheme

@Composable
fun NameTextField(
    modifier: Modifier = Modifier,
    textState: TextFieldState,
    label: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String,
    onNextClick: () -> Unit = {},
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
            imeAction = ImeAction.Next
        ),
        onKeyboardAction = KeyboardActionHandler {
            onNextClick()
        },
        supportingText = {
            if (isError) {
                Text(
                    errorMessage,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NameTextFieldPreview() {
    CraftADayTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NameTextField(
                textState = TextFieldState(),
                label = "First Name",
                placeholder = "First Name",
                isError = false,
                errorMessage = ""
            )
        }
    }
}

