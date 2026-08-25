// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.dataStructure.ListCollection
import com.den.craftaday.backend.dataStructure.Mark
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.DataFetchUseCase
import com.den.craftaday.backend.useCase.MapUseCase
import com.den.craftaday.backend.useCase.TaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    val mapUseCase: MapUseCase,
    val dataFetchUseCase: DataFetchUseCase,
) : ViewModel() {
    companion object {
        const val SUBSCRIBE_TIMEOUT = 5000L
    }

    /**
     * Set the collection id
     * @param id of the collection
     */
    fun setCollectionId(id: String) = dataFetchUseCase.setCollectionId(id)

    // ========================= Fetching all data from the fire store ============================

    /**
     * Fetch all the collections
     */
    val fetchAllCollections = dataFetchUseCase.fetchAllCollections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = DataState.Loading
        )

    /**
     * Fetch all the maps in a collection
     */
    val mapsInCollection = dataFetchUseCase.mapsInCollection
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = DataState.Loading
        )

    /**
     * Fetch all the tasks in a collection
     */
    val tasksInCollection = dataFetchUseCase.tasksInCollection
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = DataState.Loading
        )

    // ============================== Data update from fire store ======================
    /**
     * Update the task mark
     * @param task The task to update
     */
    fun onMarkClick(task: Task) {
        taskUseCase.updateTask(task.collectionId, task)
    }


    /**
     * Update a task in a collection
     * @param collectionId of the collection
     * @param task to update
     */
    fun updateTask(collectionId: String, task: Task) = taskUseCase.updateTask(collectionId, task)

    /**
     * Update a map in a collection
     * @param collectionId of the collection
     * @param map to update
     */
    fun updateMap(collectionId: String, map: ProjectMap) = mapUseCase.updateMap(collectionId, map)

    // ============================ Adding Data to fire store ==========================
    /**
     * Add a map to a collection
     * @param collectionId of the collection
     * @param title of the map
     * @param description of the map
     */
    fun addMap(collectionId: String, title: String, description: String) =
        mapUseCase.addMap(collectionId, ProjectMap(title = title, description = description))

    /**
     * Add a task to a collection
     * @param collectionId of the collection
     * @param task to add
     */
    fun addTaskData(collectionId: String, task: Task) = taskUseCase.addTask(collectionId, task)

    /**
     * Add a collection to the user
     * @param name of the collection
     */
    fun addCollection(name: String) = mapUseCase.addCollection(ListCollection(name = name))

    //  ============================= Deleting Data from fire store =====================

    /**
     * Delete a map from a collection
     * @param collectionId of the collection
     * @param map to delete
     */
    fun deleteMap(collectionId: String, map: ProjectMap) = mapUseCase.deleteMap(collectionId, map)

    /**
     * Delete a task from a collection
     * @param collectionId of the collection
     * @param task to delete
     */
    fun deleteTask(collectionId: String, task: Task) = taskUseCase.deleteTask(collectionId, task)
}
