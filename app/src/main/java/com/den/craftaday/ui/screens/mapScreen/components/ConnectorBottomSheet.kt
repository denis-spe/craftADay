// Glory be to the name of the LORD GOD of Israel
package com.den.craftaday.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.ConnectorType
import com.den.craftaday.backend.viewModels.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectorBottomSheet(
    mapId: String, // Kept for consistency if needed later, but not used for VM call now
    viewModel: MapViewModel,
    showConnectorSheet: MutableState<Boolean>,
    currentConnectorType: ConnectorType,
    sheetState: SheetState,
    scope: CoroutineScope
) {
    ModalBottomSheet(
        onDismissRequest = { showConnectorSheet.value = false },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Connector Style",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ConnectorType.entries.forEach { type ->
                val isSelected = type == currentConnectorType
                Surface(
                    onClick = {
                        scope.launch {
                            viewModel.updateConnectorType(type)
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showConnectorSheet.value = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = type.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = when (type) {
                                    ConnectorType.BEZIER -> Icons.Default.Timeline
                                    ConnectorType.STRAIGHT -> Icons.Default.LinearScale
                                    ConnectorType.STEP -> Icons.Default.Polyline
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = androidx.compose.material3.ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
