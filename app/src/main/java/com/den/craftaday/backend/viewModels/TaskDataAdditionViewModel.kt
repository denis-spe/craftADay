package com.den.craftaday.backend.viewModels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.states.TaskForm
import com.den.craftaday.backend.useCase.TaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TaskDataAdditionViewModel @Inject constructor(
    val taskUseCase: TaskUseCase
): ViewModel() {

    // Instantiate your data class state wrapper once
    private val _taskState = MutableStateFlow(TaskForm())
    val taskState: StateFlow<TaskForm> = _taskState.asStateFlow()

    // Leverage derivedStateOf to read directly through the data class properties
    val isTaskFormValid by derivedStateOf {
        val state = _taskState.value
        state.collectionId.isNotBlank() && state.title.text.isNotBlank()
    }

    /**
     * Set the collection id
     * @param id of the collection
     */
    fun setCollectionId(id: String) = _taskState.update { it.copy(collectionId = id) }

    /**
     * Add a taskEntity to a collection
     */
    fun addTaskData() {

        if (!isTaskFormValid) return

        val taskEntity = TaskEntity(
            title = _taskState.value.title.text as String,
            description = _taskState.value.description.text as String,
            chosenIcon = _taskState.value.chosenIcon,
            taskAlarmType = _taskState.value.taskAlarmType
        )

        // Add a taskEntity to the collection
        taskUseCase.addTask(_taskState.value.collectionId, taskEntity)

        // Close the form
        _taskState.update { it.copy(showForm = false) }
    }

    fun updateShowForm(showForm: Boolean) = _taskState.update { it.copy(showForm = showForm) }
}