// Glory be to the name of the LORD of host
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.DataFetchUseCase
import com.den.craftaday.backend.viewModels.HomeViewModel.Companion.SUBSCRIBE_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DataFetchViewModel @Inject constructor(
    private val dataFetchUseCase: DataFetchUseCase
) : ViewModel() {

    /**
     * Set the collection id
     * @param id of the collection
     */
    fun setCollectionId(id: String) = dataFetchUseCase.setCollectionId(id)

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
}