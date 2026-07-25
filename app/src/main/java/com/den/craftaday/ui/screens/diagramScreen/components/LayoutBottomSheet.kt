// Love the LORD your GOD with all your soul and with all your mind and with all your might
// and love your neighbor as your self
package com.den.craftaday.ui.screens.diagramScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schema
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
import com.den.craftaday.backend.dataStructure.LayoutType
import com.den.craftaday.backend.viewModels.DiagramViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutBottomSheet(
    projectId: String,
    viewModel: DiagramViewModel,
    showLayoutSheet: MutableState<Boolean>,
    currentLayoutType: LayoutType,
    sheetState: SheetState,
    scope: CoroutineScope,
    onLayoutSelected: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { showLayoutSheet.value = false },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Layout Strategy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LayoutType.entries.forEach { type ->
                val isSelected = type == currentLayoutType
                Surface(
                    onClick = {
                        scope.launch {
                            viewModel.autoLayoutTree(
                                projectId,
                                type
                            )
                            onLayoutSelected()
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showLayoutSheet.value = false
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
                                    LayoutType.TOP_DOWN -> Icons.Default.AccountTree
                                    LayoutType.LEFT_RIGHT -> Icons.Default.AccountTree // Should maybe use different icons
                                    LayoutType.RADIAL -> Icons.Default.FilterList
                                    LayoutType.GRID -> Icons.Default.Delete
                                    LayoutType.MIND_MAP -> Icons.Default.Schema
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

