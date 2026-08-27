package com.den.craftaday.backend.states

import androidx.compose.foundation.text.input.TextFieldState
import com.den.craftaday.R
import com.den.craftaday.backend.entities.types.TaskAlarmType


data class TaskForm(
    val collectionId: String = "",
    val showForm: Boolean = false,
    val title: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val taskAlarmType: TaskAlarmType = TaskAlarmType.Once(),
    val chosenIcon: Int = R.drawable.ic_task_default
)
