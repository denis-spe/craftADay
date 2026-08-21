// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.MapUseCase
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
    val mapUseCase: MapUseCase
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
            initialValue = DataState.Loading
        )

    val fetchAllMaps = authorizationUseCase.userState
        .flatMapLatest { authState ->
            if (authState is AuthState.Authenticated) {
                mapUseCase.getAllMaps().map<List<ProjectMap>, DataState<List<ProjectMap>>> { maps ->
                    DataState.Success(maps)
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
            initialValue = DataState.Loading
        )

    val fetchAllCollections = authorizationUseCase.userState
        .flatMapLatest { authState ->
            if (authState is AuthState.Authenticated) {
                mapUseCase.getAllCollections().map<List<ListCollection>, DataState<List<ListCollection>>> { collections ->
                    DataState.Success(collections)
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
            initialValue = DataState.Loading
        )

    fun addMap(collectionId: String, title: String, description: String) =
        mapUseCase.addMap(collectionId, ProjectMap(title = title, description = description))

    fun deleteMap(collectionId: String, map: ProjectMap) = mapUseCase.deleteMap(collectionId, map)

    fun updateMap(collectionId: String, map: ProjectMap) = mapUseCase.updateMap(collectionId, map)

    fun addTaskData(collectionId: String, task: Task) = taskUseCase.addTask(collectionId, task)

    fun deleteTask(collectionId: String, task: Task) = taskUseCase.deleteTask(collectionId, task)

    fun updateTask(collectionId: String, task: Task) = taskUseCase.updateTask(collectionId, task)
    
    fun addCollection(name: String) = mapUseCase.addCollection(ListCollection(name = name))
}
