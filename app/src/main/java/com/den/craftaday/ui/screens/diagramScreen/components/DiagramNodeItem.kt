// Glory be to the name of the LORD of host, The GOD of Israel.
package com.den.craftaday.ui.screens.diagramScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.den.craftaday.backend.dataStructure.DiagramNode
import androidx.core.graphics.toColorInt

@Composable
fun DiagramNodeItem(
    node: DiagramNode,
    isSelected: Boolean = false,
    onDragStart: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onAddChild: () -> Unit
) {
    val density = LocalDensity.current

    val accentColor = remember(node.color) {
        try {
            Color(node.color.toColorInt())
        } catch (e: Exception) {
            Color(0xFF6200EE) // Fallback to a default color
        }
    }

    val isCompleted = node.status == "COMPLETED"
    val isInProgress = node.status == "IN_PROGRESS"

    val priorityColor = remember(node.priority) {
        when (node.priority) {
            "URGENT" -> Color(0xFFD32F2F)
            "HIGH" -> Color(0xFFF57C00)
            "MEDIUM" -> Color(0xFF1976D2)
            else -> Color(0xFF388E3C)
        }
    }

    val shape = RoundedCornerShape(14.dp)
    val cardColor = if (node.isColorFilled) accentColor else MaterialTheme.colorScheme.surface
    val contentColor = if (node.isColorFilled) Color.White else contentColorFor(cardColor)
    
    val borderWidth = if (isSelected) 3.5.dp else if (node.isColorFilled) 0.dp else 2.dp
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else accentColor

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    with(density) { node.x.dp.roundToPx() },
                    with(density) { node.y.dp.roundToPx() }
                )
            }
            .width(200.dp)
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = cardColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .border(borderWidth, borderColor, shape)
                .pointerInput(node.id) {
                    detectTapGestures(
                        onTap = { onClick() }
                    )
                }
                .pointerInput(node.id) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dxDp = dragAmount.x / density.density
                            val dyDp = dragAmount.y / density.density
                            onDrag(dxDp, dyDp)
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // Top header: Status Toggle & Priority Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Toggle Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleStatus() }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        val icon = when {
                            isCompleted -> Icons.Default.CheckCircle
                            isInProgress -> Icons.Default.Schedule
                            else -> Icons.Default.RadioButtonUnchecked
                        }
                        
                        // Adapt icon color for filled vs outlined
                        val statusColor = if (node.isColorFilled) {
                            contentColor
                        } else {
                            when {
                                isCompleted -> Color(0xFF4CAF50)
                                isInProgress -> Color(0xFFFF9800)
                                else -> Color.Gray
                            }
                        }
                        
                        Icon(
                            imageVector = icon,
                            contentDescription = "Status",
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isCompleted -> "DONE"
                                isInProgress -> "IN PROG"
                                else -> "TODO"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    // Priority Pill
                    Surface(
                        color = if (node.isColorFilled) {
                            Color.Black.copy(alpha = 0.2f)
                        } else {
                            priorityColor.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = node.priority,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (node.isColorFilled) contentColor else priorityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Node Title
                Text(
                    text = node.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (node.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = node.description,
                        fontSize = 11.sp,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { if (isCompleted) 1f else if (isInProgress) 0.5f else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (node.isColorFilled) contentColor else accentColor,
                    trackColor = contentColor.copy(alpha = 0.2f),
                )
            }
        }

        // Quick Add Child Button (centered at bottom of node card)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp)
                .size(24.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(accentColor)
                .clickable { onAddChild() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Subtask Child",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
