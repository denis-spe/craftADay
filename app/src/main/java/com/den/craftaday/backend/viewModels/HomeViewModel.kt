// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.entities.MapEntity
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.DataFetchUseCase
import com.den.craftaday.backend.useCase.MapUseCase
import com.den.craftaday.backend.useCase.TaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
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
     * Update the taskEntity markType
     * @param taskEntity The taskEntity to update
     */
    fun onMarkClick(taskEntity: TaskEntity) {
        taskUseCase.updateTask(taskEntity.collectionId, taskEntity)
    }


    /**
     * Update a taskEntity in a collection
     * @param collectionId of the collection
     * @param taskEntity to update
     */
    fun updateTask(collectionId: String, taskEntity: TaskEntity) = taskUseCase.updateTask(collectionId, taskEntity)

    /**
     * Update a map in a collection
     * @param collectionId of the collection
     * @param map to update
     */
    fun updateMap(collectionId: String, map: MapEntity) = mapUseCase.updateMap(collectionId, map)

    // ============================ Adding Data to fire store ==========================


    /**
     * Add a taskEntity to a collection
     * @param collectionId of the collection
     * @param taskEntity to add
     */
    fun addTaskData(collectionId: String, taskEntity: TaskEntity) = taskUseCase.addTask(collectionId, taskEntity)



    //  ============================= Deleting Data from fire store =====================

    /**
     * Delete a map from a collection
     * @param collectionId of the collection
     * @param map to delete
     */
    fun deleteMap(collectionId: String, map: MapEntity) = mapUseCase.deleteMap(collectionId, map)

    /**
     * Delete a taskEntity from a collection
     * @param collectionId of the collection
     * @param taskEntity to delete
     */
    fun deleteTask(collectionId: String, taskEntity: TaskEntity) = taskUseCase.deleteTask(collectionId, taskEntity)
}
