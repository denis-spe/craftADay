// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.diagramScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.DiagramViewModel
import com.den.craftaday.ui.screens.diagramScreen.components.DiagramNodeItem
import com.den.craftaday.ui.screens.diagramScreen.components.EditTaskNodeDialog

@Composable
fun DiagramScreen(
    viewModel: DiagramViewModel
) {
    val nodesState by viewModel.nodes.collectAsStateWithLifecycle()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var editingNode by remember { mutableStateOf<DiagramNode?>(null) }
    var isCreatingRoot by remember { mutableStateOf(false) }
    var creatingChildForParentId by remember { mutableStateOf<String?>(null) }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.2f, 3f)
        offset += offsetChange
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto-Layout Tree Button
                ExtendedFloatingActionButton(
                    onClick = { viewModel.autoLayoutTree() },
                    icon = { Icon(Icons.Default.AccountTree, contentDescription = "Auto Layout") },
                    text = { Text("Auto Layout") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                // Add Root Project Node FAB
                ExtendedFloatingActionButton(
                    onClick = { isCreatingRoot = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Root Node") },
                    text = { Text("Root Project") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
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
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
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

                            val filteredNodes = when (selectedFilter) {
                                "TODO" -> allNodes.filter { it.status == "TODO" }
                                "IN_PROGRESS" -> allNodes.filter { it.status == "IN_PROGRESS" }
                                "COMPLETED" -> allNodes.filter { it.status == "COMPLETED" }
                                else -> allNodes
                            }

                             // Draw Top-to-Bottom Flowing Connectors
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                allNodes.forEach { node ->
                                    val parent = allNodes.find { it.id == node.parentId }
                                    if (parent != null) {
                                        // Parent bottom-center connection point
                                        val startX = (parent.x + 100f).dp.toPx()
                                        val startY = (parent.y + 105f).dp.toPx()

                                        // Child top-center connection point
                                        val endX = (node.x + 100f).dp.toPx()
                                        val endY = node.y.dp.toPx()

                                        // Smooth Top-to-Bottom Bezier Curve
                                        val deltaY = endY - startY
                                        val controlY1 = startY + (deltaY * 0.5f)
                                        val controlY2 = endY - (deltaY * 0.5f)

                                        val path = Path().apply {
                                            moveTo(startX, startY)
                                            cubicTo(
                                                startX, controlY1,
                                                endX, controlY2,
                                                endX, endY
                                            )
                                        }

                                        val lineColor = try {
                                            Color(android.graphics.Color.parseColor(parent.color))
                                        } catch (e: Exception) {
                                            Color.Gray
                                        }

                                        drawPath(
                                            path = path,
                                            color = lineColor.copy(alpha = 0.7f),
                                            style = Stroke(width = 3.dp.toPx())
                                        )

                                        // Draw Downward Arrowhead at child top connection point
                                        val arrowPath = Path().apply {
                                            moveTo(endX, endY)
                                            lineTo(endX - 7.dp.toPx(), endY - 12.dp.toPx())
                                            lineTo(endX + 7.dp.toPx(), endY - 12.dp.toPx())
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
                                    onMove = { x, y -> viewModel.updateNodePosition(node, x, y) },
                                    onClick = { editingNode = node },
                                    onToggleStatus = { viewModel.toggleTaskStatus(node) },
                                    onAddChild = { creatingChildForParentId = node.id }
                                )
                            }
                        }
                    }
                }
            }

            // Top Header Overlay Toolbar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Project Task Tree",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Top-to-bottom infinite node planner",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Zoom Controls & Reset View
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { scale = (scale + 0.2f).coerceAtMost(3f) }) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                            }
                            IconButton(onClick = { scale = (scale - 0.2f).coerceAtLeast(0.3f) }) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                            }
                            IconButton(onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }) {
                                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        listOf("ALL", "TODO", "IN_PROGRESS", "COMPLETED").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter.replace("_", " "),
                                        fontSize = 10.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating a Root Node
    if (isCreatingRoot) {
        EditTaskNodeDialog(
            node = null,
            isCreatingRoot = true,
            onDismiss = { isCreatingRoot = false },
            onSave = { title, description, priority, status, color ->
                viewModel.addNode(
                    title = title,
                    description = description,
                    priority = priority,
                    status = status,
                    color = color,
                    parentId = null
                )
                isCreatingRoot = false
            }
        )
    }

    // Dialog for creating a Child Node
    if (creatingChildForParentId != null) {
        EditTaskNodeDialog(
            node = null,
            isCreatingChild = true,
            onDismiss = { creatingChildForParentId = null },
            onSave = { title, description, priority, status, color ->
                viewModel.addNode(
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
                viewModel.deleteNodeAndSubtree(editingNode!!.id)
                editingNode = null
            }
        )
    }
}
