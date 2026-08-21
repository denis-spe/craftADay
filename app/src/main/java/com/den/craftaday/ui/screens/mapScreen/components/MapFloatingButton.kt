// Bless be the name of LORD GOD
package com.den.craftaday.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.ConnectorType
import com.den.craftaday.backend.dataStructure.LayoutType
import com.den.craftaday.backend.viewModels.MapViewModel

@Composable
fun MapFloatingButton(
    mapId: String, // Kept for consistency
    viewModel: MapViewModel,
    showLayoutSheet: MutableState<Boolean>,
    showConnectorSheet: MutableState<Boolean>,
    isCreatingRoot: MutableState<Boolean>,
    currentLayoutType: LayoutType,
    currentConnectorType: ConnectorType,
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
                    currentLayoutType
                ) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reorganize Layout")
            }

            IconButton(
                onClick = { showLayoutSheet.value = true },
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Layers, contentDescription = "Layout")
            }

            IconButton(
                onClick = { showConnectorSheet.value = true },
                shape = CircleShape,
            ) {
                val connectorIcon = when (currentConnectorType) {
                    ConnectorType.BEZIER -> Icons.Default.Timeline
                    ConnectorType.STRAIGHT -> Icons.Default.LinearScale
                    ConnectorType.STEP -> Icons.Default.Polyline
                }
                Icon(connectorIcon, contentDescription = "Connector Style")
            }

            FilledIconButton(
                onClick = { isCreatingRoot.value = true },
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Root MapNode")
            }
        }
    }
}
