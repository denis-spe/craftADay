// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.diagramScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.dataStructure.DiagramNode
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.DiagramViewModel
import com.den.craftaday.ui.screens.diagramScreen.components.DiagramNodeItem

@Composable
fun DiagramScreen(
    viewModel: DiagramViewModel
) {
    val nodesState by viewModel.nodes.collectAsStateWithLifecycle()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var connectMode by remember { mutableStateOf(false) }
    var connectFromId by remember { mutableStateOf<String?>(null) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        offset += offsetChange
    }

    Scaffold(
        floatingActionButton = {
            Column {
                // Connect mode toggle
                FloatingActionButton(
                    onClick = {
                        connectMode = !connectMode
                        connectFromId = null
                    },
                    containerColor = if (connectMode) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("C", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedNodeId != null) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.deleteNode(selectedNodeId!!)
                            selectedNodeId = null
                        },
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Node")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                FloatingActionButton(onClick = {
                    viewModel.addNode(
                        title = if (selectedNodeId == null) "Root Node" else "Child Node",
                        parentId = selectedNodeId,
                        x = if (selectedNodeId == null) 50f else 300f,
                        y = if (selectedNodeId == null) 300f else 300f
                    )
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Node")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .transformable(state = state)
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is DataState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(text = "Error: ${result.exception.message}", color = Color.Red)
                        }
                    }
                    is DataState.Success -> {
                        val nodes = result.data
                        
                        // Draw Connectors
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            nodes.forEach { node ->
                                val parent = nodes.find { it.id == node.parentId }
                                if (parent != null) {
                                    val parentWidth = if (parent.nodeType == "ROUNDED") 165.dp.toPx() else 120.dp.toPx()
                                    val startX = parent.x + parentWidth
                                    val startY = parent.y + 30.dp.toPx()
                                    val endX = node.x
                                    val endY = node.y + 30.dp.toPx()

                                    val path = Path().apply {
                                        moveTo(startX, startY)
                                        cubicTo(
                                            (startX + endX) / 2, startY,
                                            (startX + endX) / 2, endY,
                                            endX, endY
                                        )
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color.Gray,
                                        style = Stroke(width = 2.dp.toPx())
                                    )

                                    // Draw arrowhead
                                    val arrowPath = Path().apply {
                                        moveTo(endX, endY)
                                        lineTo(endX - 10.dp.toPx(), endY - 5.dp.toPx())
                                        lineTo(endX - 10.dp.toPx(), endY + 5.dp.toPx())
                                        close()
                                    }
                                    drawPath(path = arrowPath, color = Color.Gray)
                                }
                            }
                        }

                        // Draw Nodes
                        nodes.forEach { node ->
                            DiagramNodeItem(
                                node = node,
                                isSelected = selectedNodeId == node.id || connectFromId == node.id,
                                onMove = { x, y -> viewModel.updateNodePosition(node, x, y) },
                                onClick = {
                                    if (connectMode) {
                                        if (connectFromId == null) {
                                            connectFromId = node.id
                                        } else {
                                            // set node's parent to the previously selected node
                                            viewModel.reparentNode(node, connectFromId)
                                            connectFromId = null
                                            connectMode = false
                                        }
                                    } else {
                                        selectedNodeId = if (selectedNodeId == node.id) null else node.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
