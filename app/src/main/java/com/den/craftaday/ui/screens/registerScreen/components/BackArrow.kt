// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.registerScreen.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
fun BackArrow(backStack: NavBackStack<NavKey>, onBackClick: () -> Unit = {}) {
    FilledIconButton(
        onClick = {
            onBackClick()
            backStack.removeLastOrNull()
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
            contentDescription = "Back",
            modifier = Modifier.size(24.dp)
        )
    }
}