// Glory be to LORD GOD
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataFetchUseCase @Inject constructor(
    authorizationUseCase: AuthorizationUseCase,
    private val mapUseCase: MapUseCase,
    private val taskUseCase: TaskUseCase
) {
    private val _currentCollectionId = MutableStateFlow<String?>(null)

    fun setCollectionId(id: String) {
        _currentCollectionId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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
        }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mapsInCollection = _currentCollectionId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest emptyFlow<DataState<List<ProjectMap>>>()
            mapUseCase.getMapsInCollection(id)
                .map { DataState.Success(it) as DataState<List<ProjectMap>> }
                .catch { emit(DataState.Error(it)) }
        }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksInCollection = _currentCollectionId
        .flatMapLatest { id ->
            if (id == null) return@flatMapLatest emptyFlow<DataState<List<Task>>>()
            taskUseCase.getTasksInCollection(id)
                .map { DataState.Success(it) as DataState<List<Task>> }
                .catch { emit(DataState.Error(it)) }
        }.distinctUntilChanged()
}