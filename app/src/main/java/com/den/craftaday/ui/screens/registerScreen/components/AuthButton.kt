// Love the LORD your GOD with all your soul and with all your mind
// and with all your strenght and love your neighbor as yourself
package com.den.craftaday.ui.screens.registerScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.den.craftaday.ui.screens.AUTH_BUTTON_WIDTH
import com.den.craftaday.ui.theme.CraftADayTheme
import com.den.craftaday.ui.theme.ExtendedTheme

@Composable
fun AuthButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
){

    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = ExtendedTheme.colors.primary,
            contentColor = MaterialTheme.colorScheme.surface
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }

}

@Preview(showBackground = true)
@Composable
fun AuthButtonPreview() {
    CraftADayTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthButton(
                text = "Next",
                modifier = Modifier.fillMaxWidth(AUTH_BUTTON_WIDTH)
            ) { }
        }
    }
}
