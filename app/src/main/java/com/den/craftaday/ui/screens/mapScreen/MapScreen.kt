// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.mapScreen

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.entities.types.ConnectorType
import com.den.craftaday.backend.entities.MapNodeEntity
import com.den.craftaday.backend.entities.types.LayoutType
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.MapViewModel
import com.den.craftaday.ui.screens.mapScreen.components.ConnectorBottomSheet
import com.den.craftaday.ui.screens.mapScreen.components.MapNodeItem
import com.den.craftaday.ui.screens.mapScreen.components.EditTaskNodeDialog
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import com.den.craftaday.ui.screens.mapScreen.components.MapFloatingButton
import com.den.craftaday.ui.screens.mapScreen.components.MapTopBar
import com.den.craftaday.ui.screens.mapScreen.components.LayoutBottomSheet
import kotlinx.coroutines.delay

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
    parent: MapNodeEntity,
    node: MapNodeEntity,
    layoutType: LayoutType
): ConnectorAnchors = when (layoutType) {
    LayoutType.TOP_DOWN -> ConnectorAnchors(
        startXDp = parent.x + CARD_WIDTH_DP / 2f,
        startYDp = parent.y + CARD_HEIGHT_DP - 2f,
        endXDp = node.x + CARD_WIDTH_DP / 2f,
        endYDp = node.y
    )
    LayoutType.BOTTOM_UP -> ConnectorAnchors(
        startXDp = parent.x + CARD_WIDTH_DP / 2f,
        startYDp = parent.y + 2f,
        endXDp = node.x + CARD_WIDTH_DP / 2f,
        endYDp = node.y + CARD_HEIGHT_DP
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
    LayoutType.MIND_MAP -> {
        // Decide edge based on horizontal direction
        if (node.x > parent.x) {
            // Growing Right
            ConnectorAnchors(
                startXDp = parent.x + CARD_WIDTH_DP,
                startYDp = parent.y + CARD_HEIGHT_DP / 2f,
                endXDp = node.x,
                endYDp = node.y + CARD_HEIGHT_DP / 2f
            )
        } else {
            // Growing Left
            ConnectorAnchors(
                startXDp = parent.x,
                startYDp = parent.y + CARD_HEIGHT_DP / 2f,
                endXDp = node.x + CARD_WIDTH_DP,
                endYDp = node.y + CARD_HEIGHT_DP / 2f
            )
        }
    }
}


private fun recenter(
    nodes: List<MapNodeEntity>,
    scale: MutableFloatState,
    offset: MutableState<Offset>,
    screenWidthPx: MutableFloatState,
    screenHeightPx: MutableFloatState,
    density: Density
) {
    if (nodes.isEmpty()) return
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
fun MapScreen(
    collectionId: String,
    mapId: String,
    viewModel: MapViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Log.e("MapScreen", "Notification permission denied")
            }
        }
    )

    LaunchedEffect(Unit) {
        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check for Exact Alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("MapScreen", "Exact alarm permission missing. Requesting user intervention.")
                Toast.makeText(
                    context, 
                    "Please enable 'Alarms & Reminders' for CraftADay to ensure precise task deadlines.", 
                    Toast.LENGTH_LONG
                ).show()
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MapScreen", "Failed to open exact alarm settings", e)
                }
            } else {
                Log.d("MapScreen", "Exact alarm permission is already granted.")
            }
        }
    }

    LaunchedEffect(collectionId, mapId) {
        viewModel.setMapContext(collectionId, mapId)
    }

    val nodesState by viewModel.nodes.collectAsStateWithLifecycle()
    val currentLayoutType by viewModel.layoutType.collectAsStateWithLifecycle()
    val currentConnectorType by viewModel.connectorType.collectAsStateWithLifecycle()
    val mapState by viewModel.currentMap.collectAsStateWithLifecycle()

    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }

    var editingNode by remember { mutableStateOf<MapNodeEntity?>(null) }
    val isCreatingRoot = remember { mutableStateOf(false) }
    var creatingChildForParentId by remember { mutableStateOf<String?>(null) }

    val selectedStatusFilter = remember { mutableStateOf("ALL") }
    val selectedPriorityFilter = remember { mutableStateOf("ALL") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showLayoutSheet = remember { mutableStateOf(false) }
    val showConnectorSheet = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val screenWidthPx = remember { mutableFloatStateOf(0f) }
    val screenHeightPx = remember { mutableFloatStateOf(0f) }

    var hasInitialized by remember { mutableStateOf(false) }

    var draggingNodeId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        scale.floatValue = (scale.floatValue * zoomChange).coerceIn(0.2f, 3f)
        // Natural transformation: Zoom around the centroid point
        offset.value = (offset.value - centroid) * zoomChange + centroid + panChange
    }

    var showZoomIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(scale.floatValue, transformState.isTransformInProgress) {
        if (transformState.isTransformInProgress) {
            showZoomIndicator = true
        } else {
            // Keep visible for a bit after transform ends
            delay(1500)
            showZoomIndicator = false
        }
    }

    Scaffold(
        floatingActionButton = {
            MapFloatingButton(
                mapId = mapId,
                viewModel = viewModel,
                showLayoutSheet = showLayoutSheet,
                showConnectorSheet = showConnectorSheet,
                isCreatingRoot = isCreatingRoot,
                currentLayoutType = currentLayoutType,
                currentConnectorType = currentConnectorType
            )
        },

        topBar = {
            MapTopBar(
                mapTitle = when (val result = mapState) {
                    is DataState.Success -> result.data.title
                    else -> ""
                },
                currentLayoutType = currentLayoutType,
                currentConnectorType = currentConnectorType,
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

            // Auto-center and zoom out on initial load or layout change
            LaunchedEffect(nodesState) {
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

            // Always recenter when layout type changes to keep nodes in view
            LaunchedEffect(currentLayoutType) {
                if (hasInitialized && nodesState is DataState.Success) {
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

                            // OPTIMISTIC UI: Override positions for the dragging node
                            val effectiveNodes = remember(allNodes, draggingNodeId, dragOffset) {
                                allNodes.map { node ->
                                    if (node.id == draggingNodeId) {
                                        node.copy(
                                            x = node.x + dragOffset.x,
                                            y = node.y + dragOffset.y
                                        )
                                    } else {
                                        node
                                    }
                                }
                            }
                            val effectiveNodeMap = remember(effectiveNodes) { effectiveNodes.associateBy { it.id } }

                            val filteredNodes = remember(effectiveNodes, selectedStatusFilter.value, selectedPriorityFilter.value) {
                                effectiveNodes.filter { node ->
                                    val statusMatch = selectedStatusFilter.value == "ALL" || node.status == selectedStatusFilter.value
                                    val priorityMatch = selectedPriorityFilter.value == "ALL" || node.priority == selectedPriorityFilter.value
                                    statusMatch && priorityMatch
                                }
                            }

                            val connectorLines = remember(effectiveNodes, currentLayoutType) {
                                effectiveNodes.mapNotNull { node ->
                                    val parent = effectiveNodeMap[node.parentId ?: ""] ?: return@mapNotNull null
                                    node to parent
                                }
                            }

                                    // Draw Connectors — shape depends on the active layout algorithm and user preference
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        connectorLines.forEach { (node, parent) ->
                                            val anchors = computeConnectorAnchors(parent, node, currentLayoutType)
                                            val startX = anchors.startXDp.dp.toPx()
                                            val startY = anchors.startYDp.dp.toPx()
                                            val endX = anchors.endXDp.dp.toPx()
                                            val endY = anchors.endYDp.dp.toPx()

                                            val path = Path().apply {
                                                moveTo(startX, startY)
                                                
                                                when (currentConnectorType) {
                                                    ConnectorType.STRAIGHT -> {
                                                        lineTo(endX, endY)
                                                    }
                                                    ConnectorType.STEP -> {
                                                        when (currentLayoutType) {
                                                            LayoutType.TOP_DOWN, LayoutType.BOTTOM_UP -> {
                                                                val midY = (startY + endY) / 2f
                                                                lineTo(startX, midY)
                                                                lineTo(endX, midY)
                                                                lineTo(endX, endY)
                                                            }
                                                            LayoutType.LEFT_RIGHT, LayoutType.MIND_MAP -> {
                                                                val midX = (startX + endX) / 2f
                                                                lineTo(midX, startY)
                                                                lineTo(midX, endY)
                                                                lineTo(endX, endY)
                                                            }
                                                            else -> lineTo(endX, endY)
                                                        }
                                                    }
                                                    ConnectorType.BEZIER -> {
                                                        when (currentLayoutType) {
                                                            LayoutType.TOP_DOWN, LayoutType.BOTTOM_UP -> {
                                                                val deltaY = endY - startY
                                                                cubicTo(
                                                                    startX, startY + deltaY * 0.5f,
                                                                    endX, endY - deltaY * 0.5f,
                                                                    endX, endY
                                                                )
                                                            }
                                                            LayoutType.LEFT_RIGHT, LayoutType.MIND_MAP -> {
                                                                val deltaX = endX - startX
                                                                cubicTo(
                                                                    startX + deltaX * 0.5f, startY,
                                                                    endX - deltaX * 0.5f, endY,
                                                                    endX, endY
                                                                )
                                                            }
                                                            else -> lineTo(endX, endY)
                                                        }
                                                    }
                                                }
                                            }

                                    val lineColor = try {
                                        Color(node.color.toColorInt())
                                    } catch (_: Exception) {
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

                                // Render Map Node Cards
                            filteredNodes.forEach { node ->
                                MapNodeItem(
                                    node = node,
                                    isSelected = editingNode?.id == node.id,
                                    onDragStart = {
                                        draggingNodeId = node.id
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { dx, dy ->
                                        dragOffset += Offset(dx, dy)
                                    },
                                    onDragEnd = {
                                        val finalNode = effectiveNodeMap[node.id]
                                        if (finalNode != null) {
                                            viewModel.updateNodePosition(node, finalNode.x, finalNode.y)
                                        }
                                        draggingNodeId = null
                                        dragOffset = Offset.Zero
                                    },
                                    onClick = { editingNode = node },
                                    onToggleStatus = { viewModel.toggleTaskStatus(
                                        node
                                    ) },
                                    onAddChild = { creatingChildForParentId = node.id }
                                )
                            }
                        }
                    }
                }
            }

            // Zoom Percentage Overlay
            AnimatedVisibility(
                visible = showZoomIndicator,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(0.8f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        tonalElevation = 3.dp,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "${(scale.floatValue * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
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
            onSave = { title, description, priority, status, color, side, isColorFilled, remainder, alarmRepeat ->
                viewModel.addNode(
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    side = side,
                    parentId = null,
                    isColorFilled = isColorFilled,
                    remainder = remainder,
                    alarmRepeat = alarmRepeat
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
            onSave = { title, description, priority, status, color, side, isColorFilled, remainder, alarmRepeat ->
                viewModel.addNode(
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    side = side,
                    parentId = creatingChildForParentId,
                    isColorFilled = isColorFilled,
                    remainder = remainder,
                    alarmRepeat = alarmRepeat
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
            onSave = { title, description, priority, status, color, side, isColorFilled, remainder, alarmRepeat ->
                val node = editingNode!!.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    side = side,
                    remainder = remainder,
                    alarmRepeat = alarmRepeat,
                    isColorFilled = isColorFilled
                )

                Log.d("MapScreen", "Saving updated node: ${node.id}. Remainder: ${node.remainder?.toDate()}")

                viewModel.updateNodeDetails(
                    node = node
                )

                editingNode = null
            },
            onDelete = {
                viewModel.deleteNodeAndSubtree(
                    nodeId = editingNode!!.id
                )
                editingNode = null
            }
        )
    }

    // Modal Bottom Sheet for Layout Selection
    if (showLayoutSheet.value) {
        LayoutBottomSheet(
            mapId,
            viewModel = viewModel,
            showLayoutSheet = showLayoutSheet,
            currentLayoutType = currentLayoutType,
            sheetState = sheetState,
            scope = scope,
            onLayoutSelected = {
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

    // Modal Bottom Sheet for Connector Selection
    if (showConnectorSheet.value) {
        ConnectorBottomSheet(
            mapId = mapId,
            viewModel = viewModel,
            showConnectorSheet = showConnectorSheet,
            currentConnectorType = currentConnectorType,
            sheetState = sheetState,
            scope = scope
        )
    }
}
