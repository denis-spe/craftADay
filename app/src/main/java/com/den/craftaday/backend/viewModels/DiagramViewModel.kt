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

        // Spacing in pixels between parent and child. Tunable.
        val horizontalSpacing = 200f
        val verticalSpacing = 40f

        // Determine base position. If a parent is specified, position the child to the right of the parent.
        var baseX = x
        var baseY = y

        if (parentId != null) {
            val currentNodesState = nodes.value
            if (currentNodesState is DataState.Success) {
                val parent = currentNodesState.data.find { it.id == parentId }
                if (parent != null) {
                    baseX = parent.x + horizontalSpacing
                    baseY = parent.y + verticalSpacing
                } else {
                    baseX = if (x == 0f) 300f else x
                    baseY = if (y == 0f) 300f else y
                }
            } else {
                baseX = if (x == 0f) 300f else x
                baseY = if (y == 0f) 300f else y
            }
        } else {
            baseX = if (x == 0f) 100f else x
            baseY = if (y == 0f) 300f else y
        }

        val finalX = baseX + jitterX
        val finalY = baseY + jitterY

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
        // When changing parent, reposition child to avoid overlap (place to the right of new parent)
        val horizontalSpacing = 200f
        val verticalSpacing = 40f

        var newX = node.x
        var newY = node.y

        if (newParentId != null) {
            val currentNodesState = nodes.value
            if (currentNodesState is DataState.Success) {
                val parent = currentNodesState.data.find { it.id == newParentId }
                if (parent != null) {
                    newX = parent.x + horizontalSpacing
                    newY = parent.y + verticalSpacing
                }
            }
        }

        diagramUseCase.updateDiagramNode(node.copy(parentId = newParentId, x = newX, y = newY))
    }

    fun deleteNode(nodeId: String) {
        diagramUseCase.deleteDiagramNode(nodeId)
    }
}
