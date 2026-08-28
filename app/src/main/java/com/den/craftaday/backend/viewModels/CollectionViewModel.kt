// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.viewModels

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.states.CollectionState
import com.den.craftaday.backend.states.TaskState
import com.den.craftaday.backend.useCase.CollectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionUseCase: CollectionUseCase
) : ViewModel() {
    private val _collectionState = MutableStateFlow(CollectionState())
    val collectionState: StateFlow<CollectionState> = _collectionState.asStateFlow()

    val isCollectionFormValid by derivedStateOf {
        val state = _collectionState.value
        state.name.text.isNotBlank()
    }


    /**
     * Add a collection to the user
     */
    fun addCollection() {
        if (!isCollectionFormValid) return

        val collection = ListCollectionEntity(
            name = _collectionState.value.name.text.toString(),
            description = _collectionState.value.description.text.toString()
        )

        // Add the collection to the user
        collectionUseCase.addCollection(collection)

        // Reset the form
        _collectionState.update { it.copy(showForm = false) }
    }

    fun updateShowForm(showForm: Boolean) = _collectionState.update { it.copy(showForm = showForm) }
}