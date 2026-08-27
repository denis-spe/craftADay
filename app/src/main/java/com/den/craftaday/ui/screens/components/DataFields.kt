// Glory be to name of LORD GOD and blessed is he who comes in the name of the LORD (JESUS)
package com.den.craftaday.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TaskTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    OutlinedTextField(
        state = state,
        label = { Text("Task") },
        modifier = modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = InputTransformation.maxLength(100)
    )
}

@Composable
fun TaskDescriptionField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    OutlinedTextField(
        state = state,
        label = { Text("Description") },
        modifier = modifier,
        lineLimits = TextFieldLineLimits.MultiLine(),
        inputTransformation = InputTransformation.maxLength(1000)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarField(
    onDismiss: () -> Unit,
    onConfirm: (selectedDate: LocalDate?) -> Unit
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val selectedDate = dateState.getSelectedDate()
    val calenderType = remember { mutableStateOf("CalendarMonth") }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedDate)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = if (calenderType.value == "CalendarMonth") "Select a date" else "Insert date"
                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        calenderType.value = if (calenderType.value == "CalendarMonth")
                            "TextFields" else "CalendarMonth"
                    }
                ) {
                    Icon(
                        imageVector = if (calenderType.value == "CalendarMonth")
                            Icons.Default.CalendarMonth
                        else Icons.Default.TextFields,
                        contentDescription = null
                    )
                }
            }

            if (calenderType.value == "CalendarMonth") {
                DatePicker(
                    state = dateState
                )
            } else {
                DatePicker(
                    state = dateState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    onDismiss: () -> Unit,
    onConfirm: (selectedTime: LocalTime?) -> Unit
) {
    val time = remember { LocalTime.now() }
    val timeStyleState = remember { mutableStateOf("Clock") }

    val timeState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = if (timeStyleState.value == "Clock") "Select a time" else "Insert time"

                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        timeStyleState.value = if (timeStyleState.value == "Clock")
                            "TextFields" else "Clock"
                    }
                ) {
                    Icon(
                        imageVector = if (timeStyleState.value == "Clock")
                            Icons.Default.AccessTime else Icons.Default.TextFields,
                        contentDescription = null
                    )
                }
            }

            if (timeStyleState.value == "Clock") {
                TimePicker(
                    state = timeState
                )
            } else {
                TimeInput(
                    state = timeState
                )
            }
        }
    }
}