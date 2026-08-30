package com.den.craftaday.backend.viewModels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.den.craftaday.backend.states.TaskState
import com.den.craftaday.backend.useCase.TaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    val taskUseCase: TaskUseCase
): ViewModel() {

    // Instantiate your data class state wrapper once
    private val _taskState = MutableStateFlow(TaskState())
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

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
            collectionId = _taskState.value.collectionId,
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

    /**
     * Update the taskEntity markType
     * @param taskEntity The taskEntity to update
     */
    fun onMarkClick(taskEntity: TaskEntity) {
        taskUseCase.updateTask(
            _taskState.value.collectionId,
            taskEntity
        )
    }

    fun deleteTask() {
        _taskState.value.selectedTask?.let {
            taskUseCase.deleteTask(
                _taskState.value.collectionId,
                it
                )
        }

        // Close the form
        _taskState.update { it.copy(onShowDeleteDialog = false) }
    }

    fun currentTaskToModify(taskEntity: TaskEntity) {
        _taskState.update { it.copy(selectedTask = taskEntity, onTaskModification = true) }
    }

    fun currentTaskToDelete(taskEntity: TaskEntity) {
        _taskState.update { it.copy(selectedTask = taskEntity, onShowDeleteDialog = true) }
    }


    fun editTask() {
        _taskState.value.selectedTask?.let { taskEntity ->
            // Update a taskEntity to the collection
            taskUseCase.updateTask(_taskState.value.collectionId, taskEntity)
        }
        // Close the form
        _taskState.update { it.copy(onTaskModification = false) }
    }

    fun updateShowDeleteDialog(onShowDeleteDialog: Boolean) {
        _taskState.update { it.copy(onShowDeleteDialog = onShowDeleteDialog) }
    }

    fun updateOnModification(onTaskModification: Boolean) {
        _taskState.update { it.copy(onTaskModification = onTaskModification) }
    }

    fun onDescriptionClick(onDescriptionShow: Boolean) {
        _taskState.update { it.copy(onDescriptionShow = onDescriptionShow) }
    }

    fun onTaskAlarmClick(onTaskAlarmShow: Boolean) =
        _taskState.update { it.copy(onTaskAlarmShow = onTaskAlarmShow) }

    fun updateTaskAlarm(taskAlarmType: TaskAlarmType) =
        _taskState.update { it.copy(taskAlarmType = taskAlarmType) }
}