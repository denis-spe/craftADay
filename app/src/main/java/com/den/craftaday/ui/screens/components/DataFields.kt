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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
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

@Composable
fun CollectionTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    OutlinedTextField(
        state = state,
        label = { Text("Collection") },
        modifier = modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        labelPosition = TextFieldLabelPosition.Above(),
        inputTransformation = InputTransformation.maxLength(100)
    )
}

@Composable
fun CollectionDescriptionField(
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

@Composable
fun MapTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    TextField(
        state = state,
        label = { Text("Map") },
        modifier = modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = InputTransformation.maxLength(100)
    )
}

@Composable
fun MapDescriptionField(
    modifier: Modifier = Modifier,
    state: TextFieldState
) {
    TextField(
        state = state,
        label = { Text("Description") },
        modifier = modifier,
        lineLimits = TextFieldLineLimits.MultiLine(),
        inputTransformation = InputTransformation.maxLength(1000)
    )
}


