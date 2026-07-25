// Bless be the name of LORD GOD
package com.den.craftaday.ui.screens.diagramScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.LayoutType
import com.den.craftaday.backend.viewModels.DiagramViewModel

@Composable
fun DiagramFloatingButton(
    projectId: String,
    viewModel: DiagramViewModel,
    showLayoutSheet: MutableState<Boolean>,
    isCreatingRoot: MutableState<Boolean>,
    currentLayoutType: LayoutType,
){
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
            IconButton(onClick = {
                viewModel.autoLayoutTree(
                    projectId,
                    currentLayoutType
                ) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reorganize Layout")
            }

            IconButton(
                onClick = { showLayoutSheet.value = true },
                shape = CircleShape,
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = "Auto Layout")
            }

            FilledIconButton(
                onClick = { isCreatingRoot.value = true },
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Root Node")
            }
        }
    }
}
