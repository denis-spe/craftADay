// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.diagramScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.DiagramNode

val PRESET_COLORS = listOf(
    "#D94753", // SVG Red
    "#F68C27", // SVG Orange
    "#feda1e", // SVG Yellow
    "#3F51B5", // Indigo
    "#009688", // Teal
    "#9C27B0", // Purple
    "#4CAF50", // Green
    "#607D8B"  // Blue Grey
)

val PRIORITIES = listOf("LOW", "MEDIUM", "HIGH", "URGENT")
val STATUSES = listOf("TODO", "IN_PROGRESS", "COMPLETED")
val SIDES = listOf("LEFT", "RIGHT")

@Composable
fun EditTaskNodeDialog(
    node: DiagramNode?,
    isCreatingRoot: Boolean = false,
    isCreatingChild: Boolean = false,
    initialColor: String? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, priority: String, status: String, color: String, side: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(node) { mutableStateOf(node?.title ?: "") }
    var description by remember(node) { mutableStateOf(node?.description ?: "") }
    var priority by remember(node) { mutableStateOf(node?.priority ?: "MEDIUM") }
    var status by remember(node) { mutableStateOf(node?.status ?: "TODO") }
    var side by remember(node) { mutableStateOf(node?.side ?: "RIGHT") }
    var selectedColor by remember(node) { 
        mutableStateOf(node?.color ?: initialColor ?: "#3F51B5") 
    }

    val dialogTitle = when {
        isCreatingRoot -> "Add Root DiagramProject Node"
        isCreatingChild -> "Add Child Task Node"
        else -> "Edit Task Node"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selection
                Column {
                    Text("Priority", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        PRIORITIES.forEach { pr ->
                            FilterChip(
                                selected = priority == pr,
                                onClick = { priority = pr },
                                label = { Text(pr, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Status Selection
                Column {
                    Text("Status", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        STATUSES.forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                // Side Selection (Only for Mind Map relevance, hidden for roots)
                if (!isCreatingRoot && node?.nodeType != "ROOT") {
                    Column {
                        Text("Mind Map Side", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SIDES.forEach { sd ->
                                FilterChip(
                                    selected = side == sd,
                                    onClick = { side = sd },
                                    label = { Text(sd, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Color Selection
                Column {
                    Text("Color Tag", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        PRESET_COLORS.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColor.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, priority, status, selectedColor, side)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (node == null) "Create" else "Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null && node != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Delete Subtree")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
