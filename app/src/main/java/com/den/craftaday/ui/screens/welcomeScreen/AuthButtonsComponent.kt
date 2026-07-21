// Glory be to LORD GOD of host who made the heaven and the earth
package com.den.craftaday.ui.screens.welcomeScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH

@Composable
fun GoogleButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(
            text = "Sign in with Google",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(
            text = "Login",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RegisterButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH),
        colors = ButtonDefaults.outlinedButtonColors().copy(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = "Register",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnonymousButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
    ) {
        Text(
            text = "Anonymous",
            fontWeight = FontWeight.Bold
        )
    }
}
