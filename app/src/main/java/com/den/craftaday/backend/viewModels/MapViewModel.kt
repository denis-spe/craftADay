// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.backend.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.den.craftaday.backend.alarmManager.MapAlarmManager
import com.den.craftaday.backend.dataStructure.ConnectorType
import com.den.craftaday.helper.ReminderUtils
import java.util.UUID
import com.den.craftaday.backend.dataStructure.MapNode
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.LayoutType
import com.google.firebase.Timestamp
import com.den.craftaday.backend.states.AuthState
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.useCase.AuthorizationUseCase
import com.den.craftaday.backend.useCase.MapUseCase
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
private class LayoutNode(val node: MapNode) {
    val children = mutableListOf<LayoutNode>()
    var weight = 0f
    var x = 0f
    var y = 0f
}

/** Builds a parent->children lookup over the flat node list, keyed by node id. */
private fun buildLayoutTree(currentList: List<MapNode>): Pair<Map<String, LayoutNode>, List<LayoutNode>> {
    val nodeMap = currentList.associateBy({ it.id }, { LayoutNode(it) })
    currentList.forEach { node ->
        if (node.parentId != null) {
            nodeMap[node.parentId]?.children?.add(nodeMap[node.id]!!)
        }
    }
    val rootLayoutNodes = currentList.asSequence()
        .filter { it.parentId == null }
        .mapNotNull { nodeMap[it.id] }
        .toList()
    return nodeMap to rootLayoutNodes
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapUseCase: MapUseCase,
    val authorizationUseCase: AuthorizationUseCase,
    private val alarmManager: MapAlarmManager
) : ViewModel() {

    private val _mapId = MutableStateFlow<String?>(null)
    private val _collectionId = MutableStateFlow<String?>(null)

    fun setMapContext(collectionId: String, mapId: String) {
        _collectionId.value = collectionId
        _mapId.value = mapId
    }

    val currentMap: StateFlow<DataState<ProjectMap>> = _mapId
        .flatMapLatest { mapId ->
            val collectionId = _collectionId.value
            if (mapId == null || collectionId == null) return@flatMapLatest emptyFlow<DataState<ProjectMap>>()
            authorizationUseCase.userState.flatMapLatest { authState ->
                if (authState is AuthState.Authenticated) {
                    mapUseCase.getMap(collectionId, mapId)
                        .map { map ->
                            val m = map ?: ProjectMap()
                            
                            // Sync internal layout state with saved preference
                            try {
                                val savedLayout = LayoutType.valueOf(m.layoutType)
                                if (_layoutType.value != savedLayout) {
                                    _layoutType.value = savedLayout
                                }
                            } catch (_: Exception) {
                                Log.e("MapViewModel", "Invalid layout type saved: ${m.layoutType}")
                            }

                            // Sync connector style
                            try {
                                val savedConnector = ConnectorType.valueOf(m.connectorType)
                                if (_connectorType.value != savedConnector) {
                                    _connectorType.value = savedConnector
                                }
                            } catch (_: Exception) {
                                Log.e("MapViewModel", "Invalid connector type saved: ${m.connectorType}")
                            }
                            DataState.Success(m) as DataState<ProjectMap>
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

    val nodes: StateFlow<DataState<List<MapNode>>> = _mapId
        .flatMapLatest { mapId ->
            val collectionId = _collectionId.value
            if (mapId == null || collectionId == null) return@flatMapLatest emptyFlow<DataState<List<MapNode>>>()
            authorizationUseCase.userState.flatMapLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        Log.d("MapViewModel", "User authenticated. Starting observation for map: $mapId")
                        mapUseCase.getMapNodes(collectionId, mapId)
                            .onEach { Log.d("MapViewModel", "Fetched ${it.size} nodes for map: $mapId") }
                            .map { DataState.Success(it) as DataState<List<MapNode>> }
                            .catch {
                                Log.e("MapViewModel", "Error in map flow for map: $mapId", it)
                                emit(DataState.Error(it))
                            }
                    }
                    is AuthState.NotAuthenticated -> {
                        Log.d("MapViewModel", "User not authenticated. Emitting empty success state.")
                        kotlinx.coroutines.flow.flowOf(DataState.Success(emptyList<MapNode>()))
                    }
                    else -> {
                        Log.d("MapViewModel", "Auth state: $authState. Emitting Loading.")
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

    fun updateConnectorType(type: ConnectorType) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        Log.d("MapViewModel", "Updating connector type for map $mapId to ${type.name}")
        _connectorType.value = type
        val map = (currentMap.value as? DataState.Success)?.data
        if (map != null && map.connectorType != type.name) {
            mapUseCase.updateMap(collectionId, map.copy(connectorType = type.name))
        }
    }

    fun addNode(
        title: String,
        description: String = "",
        priority: String = "MEDIUM",
        status: String = "TODO",
        color: String = "#3F51B5",
        side: String = "RIGHT",
        parentId: String? = null,
        x: Float = 0f,
        y: Float = 0f,
        isColorFilled: Boolean,
        remainder: Timestamp?,
        alarmRepeat: String = "NONE"
    ) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        val currentList = (nodes.value as? DataState.Success<List<MapNode>>)?.data ?: emptyList()

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

        val generatedId = UUID.randomUUID().toString()

        val newNode = MapNode(
            id = generatedId,
            title = title,
            description = description,
            priority = priority,
            status = status,
            parentId = parentId,
            x = calculatedX,
            y = calculatedY,
            color = color,
            side = side,
            nodeType = if (parentId == null) "ROOT" else "TASK",
            isColorFilled = isColorFilled,
            remainder = remainder,
            alarmRepeat = alarmRepeat
        )
        mapUseCase.addMapNode(collectionId, mapId, node = newNode)

        // Schedule alarm for the new node
        remainder?.let {
            alarmManager.scheduleAlarm(
                collectionId = collectionId,
                mapId = mapId,
                nodeId = newNode.id,
                nodeTitle = newNode.title,
                timestamp = it,
                status = newNode.status
            )
        }
    }

    fun updateNodeDetails(node: MapNode) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        Log.d("MapViewModel", "updateNodeDetails called for node ${node.id}. New Remainder: ${node.remainder?.toDate()}")
        
        // Directly update using the node object passed from the UI
        mapUseCase.updateMapNode(collectionId, mapId, node = node)
        
        // Reschedule alarm
        if (node.status != "COMPLETED" && node.remainder != null) {
            alarmManager.scheduleAlarm(
                collectionId = collectionId,
                mapId = mapId,
                nodeId = node.id,
                nodeTitle = node.title,
                timestamp = node.remainder!!,
                status = node.status
            )
        } else {
            alarmManager.cancelAlarm(node.id)
        }
    }

    fun toggleTaskStatus(node: MapNode) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        val nextStatus = when (node.status) {
            "TODO" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "COMPLETED"
            "COMPLETED" -> "FAILED"
            "FAILED" -> "TODO"
            else -> "TODO"
        }
        val updatedProgress = when (nextStatus) {
            "COMPLETED" -> 1f
            "IN_PROGRESS" -> 0.5f
            else -> 0f
        }
        
        val fields = mutableMapOf<String, Any>(
            "status" to nextStatus,
            "progress" to updatedProgress
        )

        // If completed, increment stats and handle recurring alarms
        if (nextStatus == "COMPLETED") {
            mapUseCase.incrementUserStats(isSuccess = true)
            
            if (node.alarmRepeat != "NONE" && node.remainder != null) {
                val nextTimestamp = ReminderUtils.calculateNextTimestamp(node.remainder!!, node.alarmRepeat)
                // Reset recurring task for the next cycle
                fields["status"] = "TODO"
                fields["progress"] = 0f
                fields["remainder"] = nextTimestamp

                // Schedule next alarm
                alarmManager.scheduleAlarm(
                    collectionId = collectionId,
                    mapId = mapId,
                    nodeId = node.id,
                    nodeTitle = node.title,
                    timestamp = nextTimestamp,
                    status = "TODO"
                )
            } else {
                alarmManager.cancelAlarm(node.id)
            }
        }
        
        mapUseCase.updateMapNodeFields(
            collectionId,
            mapId,
            node.id,
            fields = fields
        )
    }

    fun updateNodePosition(node: MapNode, x: Float, y: Float) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        mapUseCase.updateMapNodeFields(
            collectionId,
            mapId,
            node.id,
            fields = mapOf("x" to x, "y" to y)
        )
    }

    fun reparentNode(
        node: MapNode,
        newParentId: String?
    ) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        val currentList = (nodes.value as? DataState.Success<List<MapNode>>)?.data ?: emptyList()
        var newX = node.x
        var newY = node.y

        if (newParentId != null) {
            val parent = currentList.find { it.id == newParentId }
            if (parent != null) {
                newX = parent.x
                newY = parent.y + 180f
            }
        }
        mapUseCase.updateMapNode(collectionId, mapId, node = node.copy(parentId = newParentId, x = newX, y = newY))
    }

    fun deleteNodeAndSubtree(nodeId: String) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        val currentList = (nodes.value as? DataState.Success<List<MapNode>>)?.data ?: emptyList()
        val toDelete = mutableSetOf<String>()

        fun collectSubtree(id: String) {
            toDelete.add(id)
            currentList.filter { it.parentId == id }.forEach { collectSubtree(it.id) }
        }

        collectSubtree(nodeId)
        toDelete.forEach { id ->
            mapUseCase.deleteMapNode(collectionId, mapId, nodeId = id)
            alarmManager.cancelAlarm(id)
        }
    }

    /**
     * Applies the given auto-layout algorithm to the current node tree.
     * Remembers the choice in [layoutType] so the canvas can draw
     * connectors appropriately for that layout's shape.
     */
    fun autoLayoutTree(
        layoutType: LayoutType = LayoutType.TOP_DOWN
    ) {
        val mapId = _mapId.value ?: return
        val collectionId = _collectionId.value ?: return
        val currentList = (nodes.value as? DataState.Success<List<MapNode>>)?.data ?: return
        if (currentList.isEmpty()) return

        _layoutType.value = layoutType

        // Persist the choice to the map document
        val map = (currentMap.value as? DataState.Success)?.data
        if (map != null && map.layoutType != layoutType.name) {
            mapUseCase.updateMap(collectionId, map.copy(layoutType = layoutType.name))
        }

        when (layoutType) {
            LayoutType.TOP_DOWN -> layoutTopDown(mapId, currentList)
            LayoutType.LEFT_RIGHT -> layoutLeftRight(mapId, currentList)
            LayoutType.RADIAL -> layoutRadial(mapId, currentList)
            LayoutType.GRID -> layoutGrid(mapId, currentList)
            LayoutType.MIND_MAP -> layoutMindMap(mapId, currentList)
            LayoutType.BOTTOM_UP -> layoutBottomUp(mapId, currentList)
        }
    }

    private fun applyPositions(mapId: String, nodeMap: Map<String, LayoutNode>) {
        val collectionId = _collectionId.value ?: return
        nodeMap.values.forEach { layoutNode ->
            if (layoutNode.x != layoutNode.node.x || layoutNode.y != layoutNode.node.y) {
                mapUseCase.updateMapNodeFields(
                    collectionId,
                    mapId,
                    nodeId = layoutNode.node.id,
                    fields = mapOf("x" to layoutNode.x, "y" to layoutNode.y)
                )
            }
        }
    }

    /**
     * Top-to-Bottom Auto-Layout Algorithm:
     * Calculates hierarchical widths and positions root nodes and subtrees
     * wide across the top-to-bottom layout with zero node overlaps.
     */
    private fun layoutTopDown(
        mapId: String,
        currentList: List<MapNode>
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

        applyPositions(mapId, nodeMap)
    }

    /**
     * Left-to-Right Auto-Layout Algorithm:
     * Mirror of the top-down algorithm along the other axis — levels grow
     * horizontally, siblings stack vertically with zero overlaps.
     */
    private fun layoutLeftRight(
        mapId: String,
        currentList: List<MapNode>
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

        applyPositions(mapId, nodeMap)
    }

    /**
     * Radial Auto-Layout Algorithm:
     * Places each root at the center of its own ring system and fans children
     * out over concentric circles, giving each subtree an angular slice
     * proportional to its leaf count so dense branches get more room.
     */
    private fun layoutRadial(
        mapId: String,
        currentList: List<MapNode>
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

        applyPositions(mapId, nodeMap)
    }

    /**
     * Grid Auto-Layout Algorithm:
     * Ignores hierarchy and packs every node into an even grid, ordered by
     * depth then title, for a compact overview of everything at once.
     */
    private fun layoutGrid(
        mapId: String,
        currentList: List<MapNode>
    ) {
        val cardWidthDp = 200f
        val cardHeightDp = 130f
        val gapDp = 40f
        val columns = ceil(sqrt(currentList.size.toDouble())).toInt().coerceAtLeast(1)

        val (nodeMap, _) = buildLayoutTree(currentList)
        val byId = currentList.associateBy { it.id }

        fun depthOf(node: MapNode): Int {
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

        applyPositions(mapId, nodeMap)
    }

    /**
     * Bottom-to-Top Auto-Layout Algorithm:
     * Vertical mirror of the top-down layout. Roots are at the bottom,
     * subtrees grow upwards.
     */
    private fun layoutBottomUp(
        mapId: String,
        currentList: List<MapNode>
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

        applyPositions(mapId, nodeMap)
    }

    /**
     * Mind Map Auto-Layout Algorithm:
     * Places the root node(s) in the center. Primary children are split between
     * left and right sides. Subtrees grow outwards horizontally with zero overlaps.
     */
    private fun layoutMindMap(
        mapId: String,
        currentList: List<MapNode>
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

        applyPositions(mapId, nodeMap)
    }

}
