// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.TaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    val authorizationUseCase: AuthorizationUseCase,
    val taskUseCase: TaskUseCase,
) : ViewModel() {
    companion object {
        const val SUBSCRIBE_TIMEOUT = 5000L
    }

    val fetchAllTasks = authorizationUseCase.userState
        .flatMapLatest { authState ->
            if (authState is AuthState.Authenticated) {
                taskUseCase.getAllTasks().map<List<Task>, DataState<List<Task>>> { tasks ->
                    DataState.Success(tasks)
                }
                    .catch { exception ->
                        emit(DataState.Error(exception))
                    }
            } else {
                emptyFlow()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = DataState.Loading // 2. UI starts in a Loading state immediately
        )

    fun addTaskData(task: Task) = taskUseCase.addTask(task)

    fun deleteTask(task: Task) = taskUseCase.deleteTask(task)

    fun updateTask(task: Task) = taskUseCase.updateTask(task)
}