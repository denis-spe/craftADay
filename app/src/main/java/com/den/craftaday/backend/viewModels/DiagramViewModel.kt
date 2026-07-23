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

    fun addNode(
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        status: String = "TODO",
        color: String = "#3F51B5",
        parentId: String? = null,
        x: Float = 0f,
        y: Float = 0f
    ) {
        val nodeWidth = 200f
        val verticalGap = 180f
        val horizontalGap = 220f

        val currentList = (nodes.value as? DataState.Success)?.data ?: emptyList()

        var calculatedX = x
        var calculatedY = y

        if (x == 0f && y == 0f) {
            if (parentId == null) {
                // New Root Node: place at top horizontal spread (Y = 80dp)
                val rootNodes = currentList.filter { it.parentId == null }
                val maxX = rootNodes.maxOfOrNull { it.x } ?: 40f
                calculatedX = if (rootNodes.isEmpty()) 60f else maxX + 260f
                calculatedY = 80f
            } else {
                // Child Node: place below parent (Y = parent.y + 180dp)
                val parent = currentList.find { it.id == parentId }
                if (parent != null) {
                    val siblings = currentList.filter { it.parentId == parentId }
                    if (siblings.isEmpty()) {
                        calculatedX = parent.x
                    } else {
                        val maxSiblingX = siblings.maxOf { it.x }
                        calculatedX = maxSiblingX + 240f
                    }
                    calculatedY = parent.y + 180f
                } else {
                    calculatedX = 100f
                    calculatedY = 300f
                }
            }
        }

        val newNode = DiagramNode(
            title = title,
            description = description,
            priority = priority,
            status = status,
            parentId = parentId,
            x = calculatedX,
            y = calculatedY,
            color = if (parentId == null) "#673AB7" else color,
            nodeType = if (parentId == null) "ROOT" else "TASK"
        )
        diagramUseCase.addDiagramNode(newNode)
    }

    fun updateNodeDetails(node: DiagramNode) {
        diagramUseCase.updateDiagramNode(node)
    }

    fun toggleTaskStatus(node: DiagramNode) {
        val nextStatus = when (node.status) {
            "TODO" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "COMPLETED"
            else -> "TODO"
        }
        val updatedProgress = when (nextStatus) {
            "COMPLETED" -> 1f
            "IN_PROGRESS" -> 0.5f
            else -> 0f
        }
        diagramUseCase.updateDiagramNode(node.copy(status = nextStatus, progress = updatedProgress))
    }

    fun updateNodePosition(node: DiagramNode, x: Float, y: Float) {
        diagramUseCase.updateDiagramNode(node.copy(x = x, y = y))
    }

    fun reparentNode(node: DiagramNode, newParentId: String?) {
        val currentList = (nodes.value as? DataState.Success)?.data ?: emptyList()
        var newX = node.x
        var newY = node.y

        if (newParentId != null) {
            val parent = currentList.find { it.id == newParentId }
            if (parent != null) {
                newX = parent.x
                newY = parent.y + 180f
            }
        }
        diagramUseCase.updateDiagramNode(node.copy(parentId = newParentId, x = newX, y = newY))
    }

    fun deleteNodeAndSubtree(nodeId: String) {
        val currentList = (nodes.value as? DataState.Success)?.data ?: emptyList()
        val toDelete = mutableSetOf<String>()

        fun collectSubtree(id: String) {
            toDelete.add(id)
            currentList.filter { it.parentId == id }.forEach { collectSubtree(it.id) }
        }

        collectSubtree(nodeId)
        toDelete.forEach { id ->
            diagramUseCase.deleteDiagramNode(id)
        }
    }

    fun deleteNode(nodeId: String) {
        deleteNodeAndSubtree(nodeId)
    }

    /**
     * Top-to-Bottom Auto-Layout Algorithm:
     * Calculates hierarchical widths and positions root nodes and subtrees
     * wide across the top-to-bottom layout with zero node overlaps.
     */
    fun autoLayoutTree() {
        val currentList = (nodes.value as? DataState.Success)?.data ?: return
        if (currentList.isEmpty()) return

        val cardWidthDp = 200f
        val nodeGapDp = 40f
        val levelHeightDp = 180f

        class LayoutNode(val node: DiagramNode) {
            val children = mutableListOf<LayoutNode>()
            var subtreeWidth = cardWidthDp
            var x = 0f
            var y = 0f
        }

        val nodeMap = currentList.associate { it.id to LayoutNode(it) }
        currentList.forEach { node ->
            if (node.parentId != null) {
                nodeMap[node.parentId]?.children?.add(nodeMap[node.id]!!)
            }
        }

        fun calculateSubtreeWidth(layoutNode: LayoutNode): Float {
            if (layoutNode.children.isEmpty()) {
                layoutNode.subtreeWidth = cardWidthDp
            } else {
                var sumWidth = 0f
                layoutNode.children.forEach { child ->
                    sumWidth += calculateSubtreeWidth(child)
                }
                sumWidth += (layoutNode.children.size - 1) * nodeGapDp
                layoutNode.subtreeWidth = maxOf(cardWidthDp, sumWidth)
            }
            return layoutNode.subtreeWidth
        }

        val rootNodes = currentList.filter { it.parentId == null }
        val rootLayoutNodes = rootNodes.mapNotNull { nodeMap[it.id] }
        rootLayoutNodes.forEach { calculateSubtreeWidth(it) }

        fun assignPositions(layoutNode: LayoutNode, startX: Float, currentY: Float) {
            layoutNode.y = currentY
            layoutNode.x = startX + (layoutNode.subtreeWidth / 2f) - (cardWidthDp / 2f)

            var childX = startX
            layoutNode.children.forEach { child ->
                assignPositions(child, childX, currentY + levelHeightDp)
                childX += child.subtreeWidth + nodeGapDp
            }
        }

        var currentRootX = 60f
        val startY = 80f

        rootLayoutNodes.forEach { root ->
            assignPositions(root, currentRootX, startY)
            currentRootX += root.subtreeWidth + (nodeGapDp * 1.5f)
        }

        nodeMap.values.forEach { layoutNode ->
            val updated = layoutNode.node.copy(x = layoutNode.x, y = layoutNode.y)
            if (updated.x != layoutNode.node.x || updated.y != layoutNode.node.y) {
                diagramUseCase.updateDiagramNode(updated)
            }
        }
    }

}
