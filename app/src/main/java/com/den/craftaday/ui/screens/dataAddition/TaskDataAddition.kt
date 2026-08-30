// Glory be to the name of the LORD of hosts
package com.den.craftaday.ui.screens.dataAddition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.R
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.den.craftaday.backend.states.TaskState
import com.den.craftaday.backend.viewModels.TaskViewModel
import com.den.craftaday.ui.screens.components.btn.DataAdditionBtn
import com.den.craftaday.ui.screens.components.TaskDescriptionField
import com.den.craftaday.ui.screens.components.TaskTextField
import com.den.craftaday.ui.screens.components.dialog.CalendarDialog
import com.den.craftaday.ui.screens.components.dialog.DescriptionDialog
import com.den.craftaday.ui.screens.components.dialog.TimeDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDataAddition(taskViewModel: TaskViewModel, collectionId: String) {
    val state = taskViewModel.taskState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(collectionId) {
        taskViewModel.setCollectionId(collectionId)
    }

    if (state.value.showForm) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { taskViewModel.updateShowForm(false) },
            dragHandle = {}
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.value.onTaskAlarmShow -> {
                        AlarmSelectionContent(
                            isShown = state.value.onTaskAlarmShow,
                            taskViewModel = taskViewModel,
                        )
                    }

                    else -> {
                        TaskAdditionContent(
                            state = state,
                            taskViewModel = taskViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskAdditionHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Add Task",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TaskAdditionForm(
    state: State<TaskState>,
    taskViewModel: TaskViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskTextField(
            state = state.value.title,
            modifier = Modifier.fillMaxWidth()
        )


        TaskMultiRowButtons(
            descriptionTextState = state.value.description,
            onDateShow = state.value.onTaskAlarmShow,
            onDescriptionClick = { taskViewModel.onDescriptionClick(true) },
            onDateClick = { taskViewModel.onTaskAlarmClick(true) }
        )

        DataAdditionBtn(
            label = "Add Task",
            iconResId = R.drawable.outlined_task,
            onClick = taskViewModel::addTaskData
        )
    }

    // Show the description dialog
    if (state.value.onDescriptionShow) {
        DescriptionDialog(
            label = "Description",
            text = state.value.description,
            onDisplayState = state.value.onDescriptionShow,
            onDialogDisplay = { taskViewModel.onDescriptionClick(it) }
        )
    }
}


@Composable
fun TaskAdditionContent(
    state: State<TaskState>,
    taskViewModel: TaskViewModel
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TaskAdditionHeader(
            onDismiss = { taskViewModel.updateShowForm(false) }
        )

        TaskAdditionForm(
            state = state,
            taskViewModel = taskViewModel
        )
    }
}

@Composable
fun AlarmSelectionContent(
    isShown: Boolean,
    taskViewModel: TaskViewModel
) {
    val selectedAlarmTypeState = remember { mutableStateOf<TaskAlarmType?>(null) }

    if (isShown) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlarmSelectionDialogHeader(
                onDismiss = { taskViewModel.onTaskAlarmClick(false) }
            )
            AlarmSelectionDialogForm(
                selectedAlarmTypeState = selectedAlarmTypeState
            )
            AlarmSelectionDialogButtons(
                onConfirm = {
                    if (selectedAlarmTypeState.value != null) {
                        selectedAlarmTypeState.value?.let { taskViewModel.updateTaskAlarm(it) }
                        taskViewModel.onTaskAlarmClick(false)
                    }
                },
                onDismiss = { taskViewModel.onTaskAlarmClick(false) }
            )
        }
    }
}

@Composable
private fun AlarmSelectionDialogHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Back button
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
        }

        Text(
            "Select Alarm",
            style = MaterialTheme.typography.titleLarge,
        )

    }
}

@Composable
private fun AlarmSelectionDialogForm(
    selectedAlarmTypeState: MutableState<TaskAlarmType?>
) {
    val showDatePicker = remember { mutableStateOf(false) }
    val showTimePicker = remember { mutableStateOf(false) }

    val selectedDate = remember { mutableStateOf<LocalDate?>(null) }
    val selectedTime = remember { mutableStateOf<LocalTime?>(null) }

    LaunchedEffect(selectedDate.value, selectedTime.value) {
        if (selectedDate.value != null && selectedTime.value != null) {
            val dateTime = selectedDate.value!!.atTime(selectedTime.value!!)
            val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            selectedAlarmTypeState.value = TaskAlarmType.Once(
                duration = millis
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Date and time selection",
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showDatePicker.value = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedDate.value?.toString() ?: "Select Date")
            }

            Button(
                onClick = { showTimePicker.value = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(selectedTime.value?.toString() ?: "Select Time")
            }
        }
    }


    if (showDatePicker.value) {
        CalendarDialog(
            onDismiss = { showDatePicker.value = false },
            onConfirm = {
                if (it != null) {
                    selectedDate.value = it
                }
                showDatePicker.value = false
            }
        )
    }

    if (showTimePicker.value) {
        TimeDialog(
            onDismiss = { showTimePicker.value = false },
            onConfirm = {
                if (it != null) {
                    selectedTime.value = it
                }
                showTimePicker.value = false
            }
        )
    }
}

@Composable
private fun AlarmSelectionDialogButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
        Button(onClick = onConfirm) {
            Text("Confirm")
        }
    }
}