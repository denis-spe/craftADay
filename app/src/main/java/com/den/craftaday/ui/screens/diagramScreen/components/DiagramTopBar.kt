// Glory be the name of LORD GOD of hosts
package com.den.craftaday.ui.screens.diagramScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.LayoutType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramTopBar(
    projectName: String,
    currentLayoutType: LayoutType,
    scale: MutableState<Float>,
    onRecenter: () -> Unit
) {
    TopAppBar(
        title = { Text(text = projectName) },
        actions = {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                shadowElevation = 1.dp,
                tonalElevation = 1.dp,
                shape = CircleShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scale.value = (scale.value + 0.2f).coerceAtMost(3f) }) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                    }
                    IconButton(onClick = {
                        scale.value = (scale.value - 0.2f).coerceAtLeast(0.3f)
                    }) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                    }
                    IconButton(onClick = {
                        onRecenter()
                    }) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
        )
    )
}
