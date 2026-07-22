// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AddTasksUseCase
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.GetAllTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val authorizationUseCase: AuthorizationUseCase,
    val getAllTasks: GetAllTasksUseCase,
    val addTask: AddTasksUseCase
) : ViewModel() {
    companion object {
        const val SUBSCRIBE_TIMEOUT = 5000L
    }

    val fetchAllTasks = getAllTasks().map<List<Task>, DataState<List<Task>>> {
        tasks ->
        DataState.Success(tasks)
    }
        .catch { exception ->
            emit(DataState.Error(exception))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = DataState.Loading // 2. UI starts in a Loading state immediately
        )

    fun addTaskData(task: Task) = addTask(task)
}