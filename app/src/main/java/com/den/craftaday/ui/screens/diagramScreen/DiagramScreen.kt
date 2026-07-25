// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.diagramScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.dataStructure.LayoutType
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.DiagramViewModel
import com.den.craftaday.ui.screens.diagramScreen.components.DiagramNodeItem
import com.den.craftaday.ui.screens.diagramScreen.components.EditTaskNodeDialog
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import com.den.craftaday.ui.screens.diagramScreen.components.DiagramFloatingButton
import com.den.craftaday.ui.screens.diagramScreen.components.DiagramTopBar
import com.den.craftaday.ui.screens.diagramScreen.components.LayoutBottomSheet

/** dp-space anchor points for the connector between a parent and a child node. */
private data class ConnectorAnchors(
    val startXDp: Float,
    val startYDp: Float,
    val endXDp: Float,
    val endYDp: Float
)

private const val CARD_WIDTH_DP = 200f
private const val CARD_HEIGHT_DP = 110f
private const val ARROW_GAP_DP = 4f

/**
 * Picks where the connector line touches each card, based on which
 * auto-layout algorithm is currently active — bottom-to-top for the
 * top-down tree, right-to-left for the horizontal tree, and center-to-center
 * for the radial/grid layouts where the tree shape isn't spatially implied.
 */
private fun computeConnectorAnchors(
    parent: DiagramNode,
    node: DiagramNode,
    layoutType: LayoutType
): ConnectorAnchors = when (layoutType) {
    LayoutType.TOP_DOWN -> ConnectorAnchors(
        startXDp = parent.x + CARD_WIDTH_DP / 2f,
        startYDp = parent.y + CARD_HEIGHT_DP - 2f,
        endXDp = node.x + CARD_WIDTH_DP / 2f,
        endYDp = node.y
    )
    LayoutType.LEFT_RIGHT -> ConnectorAnchors(
        startXDp = parent.x + CARD_WIDTH_DP,
        startYDp = parent.y + CARD_HEIGHT_DP / 2f,
        endXDp = node.x,
        endYDp = node.y + CARD_HEIGHT_DP / 2f
    )
    LayoutType.RADIAL, LayoutType.GRID -> ConnectorAnchors(
        startXDp = parent.x + CARD_WIDTH_DP / 2f,
        startYDp = parent.y + CARD_HEIGHT_DP / 2f,
        endXDp = node.x + CARD_WIDTH_DP / 2f,
        endYDp = node.y + CARD_HEIGHT_DP / 2f
    )
}


private fun recenter(
    nodes: List<DiagramNode>,
    scale: MutableFloatState,
    offset: MutableState<Offset>,
    screenWidthPx: MutableFloatState,
    screenHeightPx: MutableFloatState,
    density: Density
) {
    val minX = nodes.minOf { it.x }
    val minY = nodes.minOf { it.y }
    val maxX = nodes.maxOf { it.x }
    val maxY = nodes.maxOf { it.y }

    val diagramWidthDp = (maxX - minX) + CARD_WIDTH_DP
    val diagramHeightDp = (maxY - minY) + CARD_HEIGHT_DP

    val diagramCenterXEachPx = (minX + diagramWidthDp / 2f) * density.density
    val diagramCenterYEachPx = (minY + diagramHeightDp / 2f) * density.density


    val paddingPx = 100f
    val availableWidthPx = screenWidthPx.floatValue - paddingPx
    val availableHeightPx = screenHeightPx.floatValue - paddingPx

    val diagramWidthPx = diagramWidthDp * density.density
    val diagramHeightPx = diagramHeightDp * density.density

    val scaleX = availableWidthPx / diagramWidthPx
    val scaleY = availableHeightPx / diagramHeightPx
    val finalScale = minOf(scaleX, scaleY).coerceIn(0.3f, 1f)

    scale.floatValue = finalScale
    offset.value = Offset(
        (screenWidthPx.floatValue / 2f - diagramCenterXEachPx) * finalScale,
        (screenHeightPx.floatValue / 2f - diagramCenterYEachPx) * finalScale
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramScreen(
    projectId: String,
    viewModel: DiagramViewModel
) {
    LaunchedEffect(projectId) {
        viewModel.setProjectId(projectId)
    }

    val nodesState by viewModel.nodes.collectAsStateWithLifecycle()
    val currentLayoutType by viewModel.layoutType.collectAsStateWithLifecycle()
    val projectState by viewModel.currentProject.collectAsStateWithLifecycle()

    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    var editingNode by remember { mutableStateOf<DiagramNode?>(null) }
    val isCreatingRoot = remember { mutableStateOf(false) }
    var creatingChildForParentId by remember { mutableStateOf<String?>(null) }

    val selectedStatusFilter = remember { mutableStateOf("ALL") }
    val selectedPriorityFilter = remember { mutableStateOf("ALL") }

    val sheetState = rememberModalBottomSheetState()
    val showLayoutSheet = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val screenWidthPx = remember { mutableFloatStateOf(0f) }
    val screenHeightPx = remember { mutableFloatStateOf(0f) }

    var hasInitialized by remember { mutableStateOf(false) }

    val transformState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        scale.floatValue = (scale.floatValue * zoomChange).coerceIn(0.2f, 3f)
        // Natural transformation: Zoom around the centroid point
        offset.value = (offset.value - centroid) * zoomChange + centroid + panChange
    }

    Scaffold(
        floatingActionButton = {
            DiagramFloatingButton(
                projectId = projectId,
                viewModel = viewModel,
                showLayoutSheet = showLayoutSheet,
                isCreatingRoot = isCreatingRoot,
                currentLayoutType = currentLayoutType,
            )
        },

        topBar = {
            DiagramTopBar(
                projectName = when (val result = projectState) {
                    is DataState.Success -> result.data.title
                    else -> ""
                },
                currentLayoutType = currentLayoutType,
                scale = scale,
                onRecenter = {
                    if (nodesState is DataState.Success) {
                        val nodes = (nodesState as DataState.Success).data
                        if (nodes.isNotEmpty()) {
                            recenter(
                                nodes = nodes,
                                scale = scale,
                                offset = offset,
                                screenWidthPx = screenWidthPx,
                                screenHeightPx = screenHeightPx,
                                density = density
                            )
                        }
                    }
                }
            )
        }

    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            screenWidthPx.floatValue = with(density) { maxWidth.toPx() }
            screenHeightPx.floatValue = with(density) { maxHeight.toPx() }

            // Auto-center and zoom out on initial load
            LaunchedEffect(nodesState, currentLayoutType) {
                if (!hasInitialized && nodesState is DataState.Success) {
                    val nodes = (nodesState as DataState.Success).data
                    if (nodes.isNotEmpty()) {
                        recenter(
                            nodes = nodes,
                            scale = scale,
                            offset = offset,
                            screenWidthPx = screenWidthPx,
                            screenHeightPx = screenHeightPx,
                            density = density
                        )
                        hasInitialized = true
                    } else {
                        hasInitialized = true
                    }
                }
            }

            // Main Canvas Area with Pan/Zoom Gestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale.floatValue,
                            scaleY = scale.floatValue,
                            translationX = offset.value.x,
                            translationY = offset.value.y
                        )
                ) {
                    when (val result = nodesState) {
                        is DataState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is DataState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Error: ${result.exception.message}",
                                    color = Color.Red
                                )
                            }
                        }

                        is DataState.Success -> {
                            val allNodes = result.data

                            // PERFORMANCE: Pre-calculate node lookup and connectors
                            val nodeMap = remember(allNodes) { allNodes.associateBy { it.id } }
                            
                            val filteredNodes = remember(allNodes, selectedStatusFilter, selectedPriorityFilter) {
                                allNodes.filter { node ->
                                    val statusMatch = selectedStatusFilter.value == "ALL" || node.status == selectedStatusFilter.value
                                    val priorityMatch = selectedPriorityFilter.value == "ALL" || node.priority == selectedPriorityFilter.value
                                    statusMatch && priorityMatch
                                }
                            }

                            val connectorLines = remember(allNodes, currentLayoutType) {
                                allNodes.mapNotNull { node ->
                                    val parent = nodeMap[node.parentId ?: ""] ?: return@mapNotNull null
                                    node to parent
                                }
                            }

                            // Draw Connectors — shape depends on the active layout algorithm
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                connectorLines.forEach { (node, parent) ->
                                    val anchors = computeConnectorAnchors(parent, node, currentLayoutType)
                                    val startX = anchors.startXDp.dp.toPx()
                                    val startY = anchors.startYDp.dp.toPx()
                                    val endX = anchors.endXDp.dp.toPx()
                                    val endY = anchors.endYDp.dp.toPx()

                                    val path = Path().apply {
                                        moveTo(startX, startY)
                                        when (currentLayoutType) {
                                            LayoutType.TOP_DOWN -> {
                                                val deltaY = endY - startY
                                                cubicTo(
                                                    startX, startY + deltaY * 0.5f,
                                                    endX, endY - deltaY * 0.5f,
                                                    endX, endY
                                                )
                                            }
                                            LayoutType.LEFT_RIGHT -> {
                                                val deltaX = endX - startX
                                                cubicTo(
                                                    startX + deltaX * 0.5f, startY,
                                                    endX - deltaX * 0.5f, endY,
                                                    endX, endY
                                                )
                                            }
                                            LayoutType.RADIAL, LayoutType.GRID -> {
                                                lineTo(endX, endY)
                                            }
                                        }
                                    }

                                    val lineColor = try {
                                        Color(node.color.toColorInt())
                                    } catch (e: Exception) {
                                        Color.Gray
                                    }

                                    drawPath(
                                        path = path,
                                        color = lineColor.copy(alpha = 0.6f),
                                        style = Stroke(width = 2.5.dp.toPx())
                                    )

                                    // IMPROVED ARROWHEAD: only shown in Radial/Grid where direction isn't spatially implied
                                    if (currentLayoutType == LayoutType.RADIAL || currentLayoutType == LayoutType.GRID) {
                                        val angle = atan2((endY - startY), (endX - startX))
                                        val gap = ARROW_GAP_DP.dp.toPx()

                                        // The tip of the arrow slightly before the actual endX, endY
                                        val tipX = endX - gap * cos(angle)
                                        val tipY = endY - gap * sin(angle)

                                        val arrowLength = 12.dp.toPx()
                                        val arrowSpread = 0.4f
                                        val arrowPath = Path().apply {
                                            moveTo(tipX, tipY)
                                            lineTo(
                                                tipX - arrowLength * cos(angle - arrowSpread),
                                                tipY - arrowLength * sin(angle - arrowSpread)
                                            )
                                            // Slight curve/swept back effect
                                            quadraticTo(
                                                tipX - arrowLength * 0.7f * cos(angle),
                                                tipY - arrowLength * 0.7f * sin(angle),
                                                tipX - arrowLength * cos(angle + arrowSpread),
                                                tipY - arrowLength * sin(angle + arrowSpread)
                                            )
                                            close()
                                        }
                                        drawPath(path = arrowPath, color = lineColor)
                                    }
                                }
                            }

                            // Render Task Node Cards
                            filteredNodes.forEach { node ->
                                DiagramNodeItem(
                                    node = node,
                                    isSelected = editingNode?.id == node.id,
                                    onMove = { x, y -> viewModel.updateNodePosition(
                                        projectId, node, x, y) },
                                    onClick = { editingNode = node },
                                    onToggleStatus = { viewModel.toggleTaskStatus(
                                        projectId,
                                        node
                                    ) },
                                    onAddChild = { creatingChildForParentId = node.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating a Root Node
    if (isCreatingRoot.value) {
        EditTaskNodeDialog(
            node = null,
            isCreatingRoot = true,
            onDismiss = { isCreatingRoot.value = false },
            onSave = { title, description, priority, status, color ->
                viewModel.addNode(
                    projectId = projectId,
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    parentId = null
                )
                isCreatingRoot.value = false
            }
        )
    }

    // Dialog for creating a Child Node
    if (creatingChildForParentId != null) {
        val parentNode = (nodesState as? DataState.Success)?.data?.find { it.id == creatingChildForParentId }
        EditTaskNodeDialog(
            node = null,
            isCreatingChild = true,
            initialColor = parentNode?.color,
            onDismiss = { creatingChildForParentId = null },
            onSave = { title, description, priority, status, color ->
                viewModel.addNode(
                    projectId = projectId,
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    parentId = creatingChildForParentId
                )
                creatingChildForParentId = null
            }
        )
    }

    // Dialog for editing an existing Node
    if (editingNode != null) {
        EditTaskNodeDialog(
            node = editingNode,
            onDismiss = { editingNode = null },
            onSave = { title, description, priority, status, color ->
                viewModel.updateNodeDetails(
                    projectId = projectId,
                    editingNode!!.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        status = status,
                        color = color
                    )
                )
                editingNode = null
            },
            onDelete = {
                viewModel.deleteNodeAndSubtree(
                    projectId = projectId,
                    nodeId = editingNode!!.id
                )
                editingNode = null
            }
        )
    }

    // Modal Bottom Sheet for Layout Selection
    if (showLayoutSheet.value) {
        LayoutBottomSheet(
            projectId,
            viewModel = viewModel,
            showLayoutSheet = showLayoutSheet,
            currentLayoutType = currentLayoutType,
            sheetState = sheetState,
            scope = scope
        )
    }
}