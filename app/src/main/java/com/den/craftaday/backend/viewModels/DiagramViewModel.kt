// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.dataStructure.ConnectorType
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.DiagramProject
import com.den.craftaday.backend.dataStructure.LayoutType
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.DiagramUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Internal tree node used purely for layout calculations.
 * `weight` is repurposed per-algorithm (subtree width, subtree height, or leaf count).
 */
private class LayoutNode(val node: DiagramNode) {
    val children = mutableListOf<LayoutNode>()
    var weight = 0f
    var x = 0f
    var y = 0f
}

/** Builds a parent->children lookup over the flat node list, keyed by node id. */
private fun buildLayoutTree(currentList: List<DiagramNode>): Pair<Map<String, LayoutNode>, List<LayoutNode>> {
    val nodeMap = currentList.associate { it.id to LayoutNode(it) }
    currentList.forEach { node ->
        if (node.parentId != null) {
            nodeMap[node.parentId]?.children?.add(nodeMap[node.id]!!)
        }
    }
    val rootLayoutNodes = currentList.filter { it.parentId == null }.mapNotNull { nodeMap[it.id] }
    return nodeMap to rootLayoutNodes
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DiagramViewModel @Inject constructor(
    private val diagramUseCase: DiagramUseCase,
    val authorizationUseCase: AuthorizationUseCase
) : ViewModel() {

    private val _projectId = MutableStateFlow<String?>(null)

    fun setProjectId(projectId: String) {
        if (_projectId.value != projectId) {
            _projectId.value = projectId
        }
    }

    val currentProject: StateFlow<DataState<DiagramProject>> = _projectId
        .flatMapLatest { projectId ->
            if (projectId == null) return@flatMapLatest emptyFlow<DataState<DiagramProject>>()
            authorizationUseCase.userState.flatMapLatest { authState ->
                if (authState is AuthState.Authenticated) {
                    diagramUseCase.getProject(projectId)
                        .map { project ->
                            val p = project ?: DiagramProject()
                            // Sync internal layout state with saved preference
                            try {
                                val savedLayout = LayoutType.valueOf(p.layoutType)
                                if (_layoutType.value != savedLayout) {
                                    _layoutType.value = savedLayout
                                }
                            } catch (e: Exception) {
                                Log.e("DiagramViewModel", "Invalid layout type saved: ${p.layoutType}")
                            }

                            // Sync connector style
                            try {
                                val savedConnector = ConnectorType.valueOf(p.connectorType)
                                if (_connectorType.value != savedConnector) {
                                    _connectorType.value = savedConnector
                                }
                            } catch (e: Exception) {
                                Log.e("DiagramViewModel", "Invalid connector type saved: ${p.connectorType}")
                            }
                            DataState.Success(p) as DataState<DiagramProject>
                        }
                        .catch { emit(DataState.Error(it)) }
                } else {
                    emptyFlow()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataState.Loading
        )

    val nodes: StateFlow<DataState<List<DiagramNode>>> = _projectId
        .flatMapLatest { projectId ->
            if (projectId == null) return@flatMapLatest emptyFlow<DataState<List<DiagramNode>>>()
            authorizationUseCase.userState.flatMapLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        Log.d("DiagramViewModel", "User authenticated. Starting observation for project: $projectId")
                        diagramUseCase.getDiagramNodes(projectId)
                            .onEach { Log.d("DiagramViewModel", "Fetched ${it.size} nodes for project: $projectId") }
                            .map { DataState.Success(it) as DataState<List<DiagramNode>> }
                            .catch {
                                Log.e("DiagramViewModel", "Error in diagram flow for project: $projectId", it)
                                emit(DataState.Error(it))
                            }
                    }
                    is AuthState.NotAuthenticated -> {
                        Log.d("DiagramViewModel", "User not authenticated. Emitting empty success state.")
                        kotlinx.coroutines.flow.flowOf(DataState.Success(emptyList<DiagramNode>()))
                    }
                    else -> {
                        Log.d("DiagramViewModel", "Auth state: $authState. Emitting Loading.")
                        kotlinx.coroutines.flow.flowOf(DataState.Loading)
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DataState.Loading
        )

    // Tracks which auto-layout algorithm was last applied, so the canvas
    // knows how to draw connectors (vertical, horizontal, or radial).
    private val _layoutType = MutableStateFlow(LayoutType.TOP_DOWN)
    val layoutType: StateFlow<LayoutType> = _layoutType.asStateFlow()

    private val _connectorType = MutableStateFlow(ConnectorType.BEZIER)
    val connectorType: StateFlow<ConnectorType> = _connectorType.asStateFlow()

    fun updateConnectorType(projectId: String, type: ConnectorType) {
        _connectorType.value = type
        val project = (currentProject.value as? DataState.Success)?.data
        if (project != null && project.connectorType != type.name) {
            diagramUseCase.updateProject(project.copy(connectorType = type.name))
        }
    }

    fun addNode(
        projectId: String,
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        status: String = "TODO",
        color: String = "#3F51B5",
        side: String = "RIGHT",
        parentId: String? = null,
        x: Float = 0f,
        y: Float = 0f
    ) {
        val nodeWidth = 200f
        val verticalGap = 180f
        val horizontalGap = 220f

        val currentList = (nodes.value as? DataState.Success<List<DiagramNode>>)?.data ?: emptyList()

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
                        calculatedX = if (side == "LEFT") parent.x - 240f else parent.x + 240f
                    } else {
                        val maxSiblingX = siblings.maxOf { it.x }
                        calculatedX = if (side == "LEFT") maxSiblingX - 240f else maxSiblingX + 240f
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
            color = color,
            side = side,
            nodeType = if (parentId == null) "ROOT" else "TASK"
        )
        diagramUseCase.addDiagramNode( projectId, node =  newNode)
    }

    fun updateNodeDetails(projectId: String, node: DiagramNode) {
        diagramUseCase.updateDiagramNode(projectId = projectId, node = node)
    }

    fun toggleTaskStatus(projectId: String, node: DiagramNode) {
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
        diagramUseCase.updateDiagramNode(projectId = projectId, node = node.copy(status = nextStatus, progress = updatedProgress))
    }

    fun updateNodePosition(projectId: String, node: DiagramNode, x: Float, y: Float) {
        diagramUseCase.updateDiagramNode(projectId = projectId, node = node.copy(x = x, y = y))
    }

    fun reparentNode(
        projectId: String,
        node: DiagramNode,
        newParentId: String?
    ) {
        val currentList = (nodes.value as? DataState.Success<List<DiagramNode>>)?.data ?: emptyList()
        var newX = node.x
        var newY = node.y

        if (newParentId != null) {
            val parent = currentList.find { it.id == newParentId }
            if (parent != null) {
                newX = parent.x
                newY = parent.y + 180f
            }
        }
        diagramUseCase.updateDiagramNode(projectId = projectId, node = node.copy(parentId = newParentId, x = newX, y = newY))
    }

    fun deleteNodeAndSubtree(projectId: String, nodeId: String) {
        val currentList = (nodes.value as? DataState.Success<List<DiagramNode>>)?.data ?: emptyList()
        val toDelete = mutableSetOf<String>()

        fun collectSubtree(id: String) {
            toDelete.add(id)
            currentList.filter { it.parentId == id }.forEach { collectSubtree(it.id) }
        }

        collectSubtree(nodeId)
        toDelete.forEach { id ->
            diagramUseCase.deleteDiagramNode(projectId = projectId, nodeId = id)
        }
    }

    fun deleteNode(projectId: String, nodeId: String) {
        deleteNodeAndSubtree(projectId = projectId, nodeId = nodeId)
    }

    /**
     * Applies the given auto-layout algorithm to the current node tree.
     * Remembers the choice in [layoutType] so the canvas can draw
     * connectors appropriately for that layout's shape.
     */
    fun autoLayoutTree(
        projectId: String,
        layoutType: LayoutType = LayoutType.TOP_DOWN
    ) {
        val currentList = (nodes.value as? DataState.Success<List<DiagramNode>>)?.data ?: return
        if (currentList.isEmpty()) return

        _layoutType.value = layoutType

        // Persist the choice to the project document
        val project = (currentProject.value as? DataState.Success)?.data
        if (project != null && project.layoutType != layoutType.name) {
            diagramUseCase.updateProject(project.copy(layoutType = layoutType.name))
        }

        when (layoutType) {
            LayoutType.TOP_DOWN -> layoutTopDown(projectId, currentList)
            LayoutType.LEFT_RIGHT -> layoutLeftRight(projectId, currentList)
            LayoutType.RADIAL -> layoutRadial(projectId, currentList)
            LayoutType.GRID -> layoutGrid(projectId, currentList)
            LayoutType.MIND_MAP -> layoutMindMap(projectId, currentList)
            LayoutType.BOTTOM_UP -> layoutBottomUp(projectId, currentList)
        }
    }

    private fun applyPositions(projectId: String, nodeMap: Map<String, LayoutNode>) {
        nodeMap.values.forEach { layoutNode ->
            val updated = layoutNode.node.copy(x = layoutNode.x, y = layoutNode.y)
            if (updated.x != layoutNode.node.x || updated.y != layoutNode.node.y) {
                diagramUseCase.updateDiagramNode(projectId = projectId, node = updated)
            }
        }
    }

    /**
     * Top-to-Bottom Auto-Layout Algorithm:
     * Calculates hierarchical widths and positions root nodes and subtrees
     * wide across the top-to-bottom layout with zero node overlaps.
     */
    private fun layoutTopDown(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val cardWidthDp = 200f
        val nodeGapDp = 40f
        val levelHeightDp = 180f

        val (nodeMap, rootLayoutNodes) = buildLayoutTree(currentList)

        fun calculateSubtreeWidth(layoutNode: LayoutNode): Float {
            if (layoutNode.children.isEmpty()) {
                layoutNode.weight = cardWidthDp
            } else {
                var sumWidth = 0f
                layoutNode.children.forEach { child ->
                    sumWidth += calculateSubtreeWidth(child)
                }
                sumWidth += (layoutNode.children.size - 1) * nodeGapDp
                layoutNode.weight = maxOf(cardWidthDp, sumWidth)
            }
            return layoutNode.weight
        }
        rootLayoutNodes.forEach { calculateSubtreeWidth(it) }

        fun assignPositions(layoutNode: LayoutNode, startX: Float, currentY: Float) {
            layoutNode.y = currentY
            layoutNode.x = startX + (layoutNode.weight / 2f) - (cardWidthDp / 2f)

            var childX = startX
            layoutNode.children.forEach { child ->
                assignPositions(child, childX, currentY + levelHeightDp)
                childX += child.weight + nodeGapDp
            }
        }

        var currentRootX = 60f
        val startY = 80f

        rootLayoutNodes.forEach { root ->
            assignPositions(root, currentRootX, startY)
            currentRootX += root.weight + (nodeGapDp * 1.5f)
        }

        applyPositions(projectId, nodeMap)
    }

    /**
     * Left-to-Right Auto-Layout Algorithm:
     * Mirror of the top-down algorithm along the other axis — levels grow
     * horizontally, siblings stack vertically with zero overlaps.
     */
    private fun layoutLeftRight(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val cardHeightDp = 110f
        val nodeGapDp = 30f
        val levelWidthDp = 260f

        val (nodeMap, rootLayoutNodes) = buildLayoutTree(currentList)

        fun calculateSubtreeHeight(layoutNode: LayoutNode): Float {
            if (layoutNode.children.isEmpty()) {
                layoutNode.weight = cardHeightDp
            } else {
                var sumHeight = 0f
                layoutNode.children.forEach { child ->
                    sumHeight += calculateSubtreeHeight(child)
                }
                sumHeight += (layoutNode.children.size - 1) * nodeGapDp
                layoutNode.weight = maxOf(cardHeightDp, sumHeight)
            }
            return layoutNode.weight
        }
        rootLayoutNodes.forEach { calculateSubtreeHeight(it) }

        fun assignPositions(layoutNode: LayoutNode, startY: Float, currentX: Float) {
            layoutNode.x = currentX
            layoutNode.y = startY + (layoutNode.weight / 2f) - (cardHeightDp / 2f)

            var childY = startY
            layoutNode.children.forEach { child ->
                assignPositions(child, childY, currentX + levelWidthDp)
                childY += child.weight + nodeGapDp
            }
        }

        var currentRootY = 60f
        val startX = 60f

        rootLayoutNodes.forEach { root ->
            assignPositions(root, currentRootY, startX)
            currentRootY += root.weight + (nodeGapDp * 1.5f)
        }

        applyPositions(projectId, nodeMap)
    }

    /**
     * Radial Auto-Layout Algorithm:
     * Places each root at the center of its own ring system and fans children
     * out over concentric circles, giving each subtree an angular slice
     * proportional to its leaf count so dense branches get more room.
     */
    private fun layoutRadial(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val ringRadiusStepDp = 240f
        val centerX = 900f
        val centerY = 700f

        val (nodeMap, rootLayoutNodes) = buildLayoutTree(currentList)

        fun countLeaves(layoutNode: LayoutNode): Float {
            val leaves = if (layoutNode.children.isEmpty()) {
                1f
            } else {
                layoutNode.children.sumOf { countLeaves(it).toDouble() }.toFloat()
            }
            layoutNode.weight = leaves
            return leaves
        }
        rootLayoutNodes.forEach { countLeaves(it) }
        val totalLeaves = rootLayoutNodes.sumOf { it.weight.toDouble() }.toFloat().coerceAtLeast(1f)

        fun assignPositions(layoutNode: LayoutNode, startAngleDeg: Float, spanDeg: Float, depth: Int) {
            val angleDeg = startAngleDeg + spanDeg / 2f
            val radius = depth * ringRadiusStepDp
            val radians = Math.toRadians(angleDeg.toDouble())
            layoutNode.x = centerX + (radius * cos(radians)).toFloat()
            layoutNode.y = centerY + (radius * sin(radians)).toFloat()

            var childAngleCursor = startAngleDeg
            layoutNode.children.forEach { child ->
                val childSpan = spanDeg * (child.weight / layoutNode.weight)
                assignPositions(child, childAngleCursor, childSpan, depth + 1)
                childAngleCursor += childSpan
            }
        }

        var angleCursor = 0f
        rootLayoutNodes.forEach { root ->
            val rootSpan = 360f * (root.weight / totalLeaves)
            assignPositions(root, angleCursor, rootSpan, 1)
            angleCursor += rootSpan
        }

        // Single-root trees look best centered rather than offset onto ring 1.
        if (rootLayoutNodes.size == 1) {
            rootLayoutNodes[0].x = centerX
            rootLayoutNodes[0].y = centerY
        }

        applyPositions(projectId, nodeMap)
    }

    /**
     * Grid Auto-Layout Algorithm:
     * Ignores hierarchy and packs every node into an even grid, ordered by
     * depth then title, for a compact overview of everything at once.
     */
    private fun layoutGrid(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val cardWidthDp = 200f
        val cardHeightDp = 130f
        val gapDp = 40f
        val columns = ceil(sqrt(currentList.size.toDouble())).toInt().coerceAtLeast(1)

        val (nodeMap, _) = buildLayoutTree(currentList)
        val byId = currentList.associateBy { it.id }

        fun depthOf(node: DiagramNode): Int {
            var depth = 0
            var current = node
            while (current.parentId != null) {
                val parent = byId[current.parentId] ?: break
                depth++
                current = parent
            }
            return depth
        }

        val ordered = currentList.sortedWith(compareBy({ depthOf(it) }, { it.title }))

        ordered.forEachIndexed { index, node ->
            val row = index / columns
            val col = index % columns
            val layoutNode = nodeMap[node.id] ?: return@forEachIndexed
            layoutNode.x = 60f + col * (cardWidthDp + gapDp)
            layoutNode.y = 80f + row * (cardHeightDp + gapDp)
        }

        applyPositions(projectId, nodeMap)
    }

    /**
     * Bottom-to-Top Auto-Layout Algorithm:
     * Vertical mirror of the top-down layout. Roots are at the bottom,
     * subtrees grow upwards.
     */
    private fun layoutBottomUp(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val cardWidthDp = 200f
        val nodeGapDp = 40f
        val levelHeightDp = 180f

        val (nodeMap, rootLayoutNodes) = buildLayoutTree(currentList)

        fun calculateSubtreeWidth(layoutNode: LayoutNode): Float {
            if (layoutNode.children.isEmpty()) {
                layoutNode.weight = cardWidthDp
            } else {
                var sumWidth = 0f
                layoutNode.children.forEach { child ->
                    sumWidth += calculateSubtreeWidth(child)
                }
                sumWidth += (layoutNode.children.size - 1) * nodeGapDp
                layoutNode.weight = maxOf(cardWidthDp, sumWidth)
            }
            return layoutNode.weight
        }
        rootLayoutNodes.forEach { calculateSubtreeWidth(it) }

        fun assignPositions(layoutNode: LayoutNode, startX: Float, currentY: Float) {
            layoutNode.y = currentY
            layoutNode.x = startX + (layoutNode.weight / 2f) - (cardWidthDp / 2f)

            var childX = startX
            layoutNode.children.forEach { child ->
                // GROW UP: Decreasing Y coordinate
                assignPositions(child, childX, currentY - levelHeightDp)
                childX += child.weight + nodeGapDp
            }
        }

        var currentRootX = 60f
        // Start at a reasonably high bottom Y so growth doesn't hit negative space immediately
        val bottomY = 1200f

        rootLayoutNodes.forEach { root ->
            assignPositions(root, currentRootX, bottomY)
            currentRootX += root.weight + (nodeGapDp * 1.5f)
        }

        applyPositions(projectId, nodeMap)
    }

    /**
     * Mind Map Auto-Layout Algorithm:
     * Places the root node(s) in the center. Primary children are split between
     * left and right sides. Subtrees grow outwards horizontally with zero overlaps.
     */
    private fun layoutMindMap(
        projectId: String,
        currentList: List<DiagramNode>
    ) {
        val cardHeightDp = 110f
        val nodeGapDp = 30f
        val levelWidthDp = 260f
        val centerX = 900f
        val startY = 400f

        val (nodeMap, rootLayoutNodes) = buildLayoutTree(currentList)

        fun calculateSubtreeHeight(layoutNode: LayoutNode): Float {
            if (layoutNode.children.isEmpty()) {
                layoutNode.weight = cardHeightDp
            } else {
                var sumHeight = 0f
                layoutNode.children.forEach { child ->
                    sumHeight += calculateSubtreeHeight(child)
                }
                sumHeight += (layoutNode.children.size - 1) * nodeGapDp
                layoutNode.weight = maxOf(cardHeightDp, sumHeight)
            }
            return layoutNode.weight
        }

        fun assignPositions(layoutNode: LayoutNode, startY: Float, currentX: Float, direction: Float) {
            layoutNode.x = currentX
            layoutNode.y = startY + (layoutNode.weight / 2f) - (cardHeightDp / 2f)

            var childY = startY
            layoutNode.children.forEach { child ->
                assignPositions(child, childY, currentX + (direction * levelWidthDp), direction)
                childY += child.weight + nodeGapDp
            }
        }

        var currentRootY = startY
        rootLayoutNodes.forEach { root ->
            val rightChildren = root.children.filter { it.node.side == "RIGHT" }
            val leftChildren = root.children.filter { it.node.side == "LEFT" }

            val rightHeight = if (rightChildren.isEmpty()) cardHeightDp else rightChildren.sumOf { calculateSubtreeHeight(it).toDouble() }.toFloat() + (rightChildren.size - 1) * nodeGapDp
            val leftHeight = if (leftChildren.isEmpty()) cardHeightDp else leftChildren.sumOf { calculateSubtreeHeight(it).toDouble() }.toFloat() + (leftChildren.size - 1) * nodeGapDp
            
            root.weight = maxOf(rightHeight, leftHeight)
            root.x = centerX
            root.y = currentRootY + (root.weight / 2f) - (cardHeightDp / 2f)

            // Layout Right side
            if (rightChildren.isNotEmpty()) {
                var rightStartY = root.y + (cardHeightDp / 2f) - rightHeight / 2f
                rightChildren.forEach { child ->
                    assignPositions(child, rightStartY, centerX + levelWidthDp, 1f)
                    rightStartY += child.weight + nodeGapDp
                }
            }

            // Layout Left side
            if (leftChildren.isNotEmpty()) {
                var leftStartY = root.y + (cardHeightDp / 2f) - leftHeight / 2f
                leftChildren.forEach { child ->
                    assignPositions(child, leftStartY, centerX - levelWidthDp, -1f)
                    leftStartY += child.weight + nodeGapDp
                }
            }

            currentRootY += root.weight + (nodeGapDp * 1.5f)
        }

        applyPositions(projectId, nodeMap)
    }

}