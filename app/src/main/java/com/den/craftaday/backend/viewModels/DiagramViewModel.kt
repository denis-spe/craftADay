// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.DiagramUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DiagramViewModel @Inject constructor(
    private val diagramUseCase: DiagramUseCase
) : ViewModel() {

    val nodes: StateFlow<DataState<List<DiagramNode>>> = diagramUseCase.getDiagramNodes()
        .onEach { Log.d("DiagramViewModel", "Fetched ${it.size} nodes") }
        .map { DataState.Success(it) as DataState<List<DiagramNode>> }
        .catch { 
            Log.e("DiagramViewModel", "Error fetching nodes", it)
            emit(DataState.Error(it)) 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataState.Loading
        )

    fun addNode(title: String, parentId: String? = null, x: Float = 0f, y: Float = 0f) {
        // Add jitter to avoid perfect overlaps
        val jitterX = Random.nextFloat() * 40f - 20f
        val jitterY = Random.nextFloat() * 40f - 20f

        val finalX = (if (x == 0f && parentId == null) 100f else x) + jitterX
        val finalY = (if (y == 0f && parentId == null) 300f else y) + jitterY

        Log.d("DiagramViewModel", "Adding node: $title at ($finalX, $finalY) with parent: $parentId")

        val newNode = DiagramNode(
            title = title,
            parentId = parentId,
            x = finalX,
            y = finalY,
            color = if (parentId == null) "#D94753" else "#F68C27",
            nodeType = if (parentId == null) "ROUNDED" else "RECT"
        )
        diagramUseCase.addDiagramNode(newNode)
    }

    fun updateNodePosition(node: DiagramNode, x: Float, y: Float) {
        diagramUseCase.updateDiagramNode(node.copy(x = x, y = y))
    }

    fun reparentNode(node: DiagramNode, newParentId: String?) {
        diagramUseCase.updateDiagramNode(node.copy(parentId = newParentId))
    }

    fun deleteNode(nodeId: String) {
        diagramUseCase.deleteDiagramNode(nodeId)
    }
}
