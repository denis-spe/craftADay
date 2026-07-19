// Glory be to LORD GOD of host who made the heaven and the earth
package com.den.craftaday.ui.screens.welcomeScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH

@Composable
fun GoogleButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(text = "Sign in with Google")
    }
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(text = "Login")
    }
}

@Composable
fun RegisterButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(text = "Register")
    }
}

@Composable
fun AnonymousButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(text = "Anonymous")
    }
}
