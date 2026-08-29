// Glory be to the LORD our GOD
package com.den.craftaday.ui.screens.homeScreen.taskList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.entities.types.MarkType
import com.den.craftaday.backend.entities.types.TaskAlarmType
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.DataFetchViewModel
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.helper.CurrentTimeChange
import com.den.craftaday.helper.TimeChange

@Composable
fun TaskList(
    dataFetchViewModel: DataFetchViewModel,
    onMarkClick: (taskEntity: TaskEntity) -> Unit
) {
    val state by dataFetchViewModel.tasksInCollection.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is DataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is DataState.Error -> Text(
                "Error loading tasks",
                modifier = Modifier.align(Alignment.Center)
            )

            is DataState.Success -> {
                val tasks = (state as DataState.Success).data
                if (tasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = com.den.craftaday.R.drawable.empty_tasks),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No tasks in this collection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskItem(taskEntity = task, onMarkClick = onMarkClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    taskEntity: TaskEntity,
    onMarkClick: (taskEntity: TaskEntity) -> Unit
) {
    val mark = taskEntity.markType

    val color = remember(mark) {
        when (mark) {
            MarkType.Done -> Color(0xFF388E3C)
            MarkType.InProgress -> Color(0xFFFFA500)
            MarkType.Failed -> Color(0xFFB00020)
            MarkType.Initial -> Color.Gray
        }
    }
    val textDecoration = remember(mark) {
        when (mark) {
            MarkType.Done -> TextDecoration.LineThrough
            MarkType.InProgress -> TextDecoration.None
            MarkType.Failed -> TextDecoration.LineThrough
            MarkType.Initial -> TextDecoration.None
        }
    }

    val deadlineTimeChange = remember(taskEntity.taskAlarmType) {
        val timeChange = when (val alarm = taskEntity.taskAlarmType) {
            is TaskAlarmType.SpecificWeekDay -> alarm.dateTimes.CurrentTimeChange
            is TaskAlarmType.SpecificDate -> alarm.dateTimes.CurrentTimeChange
            else -> alarm.primaryTimestamp.CurrentTimeChange
        }

        val label = when (timeChange) {
            is TimeChange.Years -> "${timeChange.value} years"
            is TimeChange.Months -> "${timeChange.value} months"
            is TimeChange.Days -> "${timeChange.value} days"
            is TimeChange.Hours -> "${timeChange.value} hours"
            is TimeChange.Minutes -> "${timeChange.value} minutes"
            is TimeChange.SpecificDate -> timeChange.value
            is TimeChange.SpecificWeekDay -> timeChange.value
            TimeChange.JustNow -> "just now"
        }

        if (label == "just now") label else "Deadline in $label"
    }

    val markTypeState = remember(taskEntity.markType) {
        when (mark) {
            MarkType.Done -> MarkType.Failed
            MarkType.InProgress -> MarkType.Done
            MarkType.Failed -> MarkType.Initial
            MarkType.Initial -> MarkType.InProgress
        }
    }

    var isVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            onMarkClick(taskEntity.copy(markType = markTypeState))
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header icon reflects the taskEntity's actual current state, so it's the
                // only icon in this row that should animate.

                Image(
                    painter = painterResource(id = taskEntity.chosenIcon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        taskEntity.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = textDecoration
                    )

                    Text(
                        text = deadlineTimeChange,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                StatusIcon(
                    markType = mark,
                    animate = true,
                    modifier = Modifier.size(28.dp)
                )
                IconButton(
                    onClick = {
                        isVisible = !isVisible
                    }
                ) {
                    Icon(
                        imageVector = if (isVisible)
                            Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (taskEntity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = taskEntity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TaskIconToggleButtons(
                isVisible = isVisible,
                taskEntity = taskEntity,
                onMarkChange = onMarkClick
            )
        }
    }
}

/**
 * Renders the status icon for [markType]. When [animate] is false, a static
 * (non-animated) drawable is used instead of inflating an AnimatedVectorDrawable —
 * avoids running an infinite-repeat animator for icons that aren't the active state
 * (e.g. unselected toggle buttons in a long, recycled list).
 */
@Composable
fun StatusIcon(
    markType: MarkType,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (markType) {
        MarkType.Initial -> com.den.craftaday.R.drawable.avd_task_initial
        MarkType.Done -> com.den.craftaday.R.drawable.avd_task_done
        else -> com.den.craftaday.R.drawable.avd_task_failed
    }

    if (markType == MarkType.InProgress) {
        Image(
            painter = painterResource(id = com.den.craftaday.R.drawable.avd_task_in_progress),
            contentDescription = null,
            modifier = modifier
        )
        return
    }

    // avd_task_* are <animated-vector> roots — painterResource() doesn't support
    // that root type and throws IllegalArgumentException at runtime. Always go
    // through animatedVectorResource()/rememberAnimatedVectorPainter(); for the
    // "static" case we just never flip atEnd, so no transition fires and the
    // drawable renders its end frame once with no ongoing animator.
    val image = AnimatedImageVector.animatedVectorResource(drawableRes)


    // Simplified animation state to avoid double-recomposition on start.
    val atEnd by remember(markType, animate) { mutableStateOf(markType != MarkType.InProgress && !animate) }
    val painter = rememberAnimatedVectorPainter(image, atEnd || animate)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun TaskIconToggleButtons(
    isVisible: Boolean,
    taskEntity: TaskEntity,
    onMarkChange: (TaskEntity) -> Unit
) {
    Column {
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusButton(
                        selected = taskEntity.markType == MarkType.Initial,
                        markType = MarkType.Initial,
                        label = "Initial",
                        color = Color(0xFF777672),
                        onClick = remember(taskEntity, onMarkChange) { { onMarkChange(taskEntity.copy(markType = MarkType.Initial)) } }
                    )
                    StatusButton(
                        selected = taskEntity.markType == MarkType.InProgress,
                        markType = MarkType.InProgress,
                        label = "Doing",
                        color = Color(0xFFFFA500),
                        onClick = remember(taskEntity, onMarkChange) { { onMarkChange(taskEntity.copy(markType = MarkType.InProgress)) } }
                    )
                    StatusButton(
                        selected = taskEntity.markType == MarkType.Done,
                        markType = MarkType.Done,
                        label = "Done",
                        color = Color(0xFF388E3C),
                        onClick = remember(taskEntity, onMarkChange) { { onMarkChange(taskEntity.copy(markType = MarkType.Done)) } }
                    )
                    StatusButton(
                        selected = taskEntity.markType == MarkType.Failed,
                        markType = MarkType.Failed,
                        label = "Failed",
                        color = Color(0xFFB00020),
                        onClick = remember(taskEntity, onMarkChange) { { onMarkChange(taskEntity.copy(markType = MarkType.Failed)) } }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusButton(
    selected: Boolean,
    markType: MarkType,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Only the selected toggle button animates; the rest render statically,
        // which cuts each list row from 5 concurrent infinite AVD animators down to 1-2.
        StatusIcon(
            markType = markType,
            animate = selected,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) color else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}