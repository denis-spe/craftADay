// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.mapScreen.components

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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.den.craftaday.backend.dataStructure.MapNode
import com.google.firebase.Timestamp
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.lazy.LazyColumn

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
val REPEAT_OPTIONS = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY")

@Composable
fun EditTaskNodeDialog(
    node: MapNode?,
    isCreatingRoot: Boolean = false,
    isCreatingChild: Boolean = false,
    initialColor: String? = null,
    onDismiss: () -> Unit,
    onSave: (title: String,
             description: String,
             priority: String,
             status: String,
             color: String, side: String,
             isColorFilled: Boolean,
             remainder: Timestamp?,
             alarmRepeat: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember(node) { mutableStateOf(node?.title ?: "") }
    var description by remember(node) { mutableStateOf(node?.description ?: "") }
    var priority by remember(node) { mutableStateOf(node?.priority ?: "MEDIUM") }
    var status by remember(node) { mutableStateOf(node?.status ?: "TODO") }
    var side by remember(node) { mutableStateOf(node?.side ?: "RIGHT") }
    var alarmRepeat by remember(node) { mutableStateOf(node?.alarmRepeat ?: "NONE") }
    var isColorFilled by remember(node) { mutableStateOf(node?.isColorFilled ?: false) }
    var remainder by remember(node) { mutableStateOf(node?.remainder) }
    var selectedColor by remember(node) { 
        mutableStateOf(node?.color ?: initialColor ?: "#3F51B5")
    }

    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val dateFormatter = remember(is24Hour) { 
        val pattern = if (is24Hour) "MMM dd, yyyy HH:mm" else "MMM dd, yyyy hh:mm a"
        SimpleDateFormat(pattern, Locale.getDefault()) 
    }

    fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance().apply {
            time = remainder?.toDate() ?: java.util.Date()
        }
        
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                currentCalendar.set(Calendar.YEAR, year)
                currentCalendar.set(Calendar.MONTH, month)
                currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        currentCalendar.set(Calendar.MINUTE, minute)
                        remainder = Timestamp(currentCalendar.time)
                    },
                    currentCalendar.get(Calendar.HOUR_OF_DAY),
                    currentCalendar.get(Calendar.MINUTE),
                    is24Hour
                ).show()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }


    val dialogTitle = when {
        isCreatingRoot -> "Add Root Map Node"
        isCreatingChild -> "Add Child Task Node"
        else -> "Edit Task Node"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
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
                                    label = {
                                        Text(
                                            pr,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                item {
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
                                    label = {
                                        Text(
                                            st.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                item {
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
                                        label = {
                                            Text(
                                                sd,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Remainder / Deadline Selection
                    Column {
                        Text("Reminder", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { showDateTimePicker() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (remainder != null) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = remainder?.let { dateFormatter.format(it.toDate()) } ?: "No reminder set",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (remainder != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            if (remainder != null) {
                                IconButton(
                                    onClick = { 
                                        remainder = null
                                        alarmRepeat = "NONE"
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        // Past time warning
                        remainder?.let {
                            if (it.toDate().time < System.currentTimeMillis()) {
                                Text(
                                    text = "Warning: Selected time is in the past.",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // Alarm Repeat Selection
                    Column(modifier = Modifier.alpha(if (remainder != null) 1f else 0.5f)) {
                        Text("Repeat", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            REPEAT_OPTIONS.forEach { opt ->
                                FilterChip(
                                    selected = alarmRepeat == opt,
                                    onClick = { if (remainder != null) alarmRepeat = opt },
                                    label = {
                                        Text(
                                            opt,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    enabled = remainder != null || opt == "NONE",
                                    colors = FilterChipDefaults.filterChipColors(
                                        disabledContainerColor = Color.Transparent,
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                }

                item {
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
                                val color = Color(hex.toColorInt())
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    // Fill Color Toggle (Entire Row Toggleable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .toggleable(
                                value = isColorFilled,
                                onValueChange = { isColorFilled = it },
                                role = Role.Switch
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Fill Background",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isColorFilled,
                            onCheckedChange = null, // Handled by the Row's toggleable modifier
                            colors = SwitchDefaults.colors().copy(
                                checkedThumbColor = Color(selectedColor.toColorInt()),
                                checkedBorderColor = Color(selectedColor.toColorInt())
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title,
                            description,
                            priority,
                            status,
                            selectedColor,
                            side,
                            isColorFilled,
                            remainder,
                            alarmRepeat)
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
