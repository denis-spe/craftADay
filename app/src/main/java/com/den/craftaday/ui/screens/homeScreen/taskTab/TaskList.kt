// Glory be to the LORD our GOD
package com.den.craftaday.ui.screens.homeScreen.taskTab

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
import androidx.compose.runtime.LaunchedEffect
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
import com.den.craftaday.backend.dataStructure.Mark
import com.den.craftaday.backend.dataStructure.Reset
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.helper.CurrentTimeChange
import com.den.craftaday.helper.TimeChange

@Composable
fun TaskList(
    homeViewModel: HomeViewModel,
    onMarkClick: (task: Task) -> Unit
) {
    val state by homeViewModel.tasksInCollection.collectAsStateWithLifecycle()

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
                    Text(
                        "No tasks in this collection.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskItem(task = task, onMarkClick = onMarkClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onMarkClick: (task: Task) -> Unit
) {
    val mark = task.mark

    val color = remember(mark) {
        when (mark) {
            Mark.Done -> Color(0xFF388E3C)
            Mark.InProgress -> Color(0xFFFFA500)
            Mark.Failed -> Color(0xFFB00020)
            Mark.Initial -> Color.Gray
        }
    }
    val textDecoration = remember(mark) {
        when (mark) {
            Mark.Done -> TextDecoration.LineThrough
            Mark.InProgress -> TextDecoration.None
            Mark.Failed -> TextDecoration.LineThrough
            Mark.Initial -> TextDecoration.None
        }
    }

    val deadlineTimeChange = remember(task.onReset) {
        if (task.onReset is Reset.No) return@remember null
        val timeChange = when (task.onReset) {
            is Reset.Once -> task.onReset.dateTime.CurrentTimeChange
            is Reset.Daily -> task.onReset.dateTime.CurrentTimeChange
            is Reset.Weekly -> task.onReset.dateTime.CurrentTimeChange
            is Reset.Monthly -> task.onReset.dateTime.CurrentTimeChange
            is Reset.Yearly -> task.onReset.dateTime.CurrentTimeChange
        }

        when (timeChange) {
            is TimeChange.Months -> "Deadline in ${timeChange.value} months"
            is TimeChange.Years -> "Deadline in ${timeChange.value} years"
            is TimeChange.Days -> "Deadline in ${timeChange.value} days"
            is TimeChange.Hours -> "Deadline in ${timeChange.value} hours"
            is TimeChange.Minutes -> "Deadline in ${timeChange.value} minutes"
            is TimeChange.JustNow -> "just now"
        }
    }

    val markState = remember(task.mark) {
        when (mark) {
            Mark.Done -> Mark.Failed
            Mark.InProgress -> Mark.Done
            Mark.Failed -> Mark.Initial
            Mark.Initial -> Mark.InProgress
        }
    }

    var isVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            onMarkClick(task.copy(mark = markState))
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header icon reflects the task's actual current state, so it's the
                // only icon in this row that should animate.

                Image(
                    painter = painterResource(id = task.chosenIcon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = textDecoration
                    )

                    deadlineTimeChange?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                StatusIcon(
                    mark = mark,
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

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TaskIconToggleButtons(
                isVisible = isVisible,
                task = task,
                onMarkChange = onMarkClick
            )
        }
    }
}

/**
 * Renders the status icon for [mark]. When [animate] is false, a static
 * (non-animated) drawable is used instead of inflating an AnimatedVectorDrawable —
 * avoids running an infinite-repeat animator for icons that aren't the active state
 * (e.g. unselected toggle buttons in a long, recycled list).
 */
@Composable
fun StatusIcon(
    mark: Mark,
    animate: Boolean,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (mark) {
        Mark.Initial -> com.den.craftaday.R.drawable.avd_task_initial
        Mark.Done -> com.den.craftaday.R.drawable.avd_task_done
        else -> com.den.craftaday.R.drawable.avd_task_failed
    }

    if (mark == Mark.InProgress) {
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
    val atEnd by remember(mark, animate) { mutableStateOf(mark != Mark.InProgress && !animate) }
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
    task: Task,
    onMarkChange: (Task) -> Unit
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
                        selected = task.mark == Mark.Initial,
                        mark = Mark.Initial,
                        label = "Initial",
                        color = Color(0xFF777672),
                        onClick = remember(task, onMarkChange) { { onMarkChange(task.copy(mark = Mark.Initial)) } }
                    )
                    StatusButton(
                        selected = task.mark == Mark.InProgress,
                        mark = Mark.InProgress,
                        label = "Doing",
                        color = Color(0xFFFFA500),
                        onClick = remember(task, onMarkChange) { { onMarkChange(task.copy(mark = Mark.InProgress)) } }
                    )
                    StatusButton(
                        selected = task.mark == Mark.Done,
                        mark = Mark.Done,
                        label = "Done",
                        color = Color(0xFF388E3C),
                        onClick = remember(task, onMarkChange) { { onMarkChange(task.copy(mark = Mark.Done)) } }
                    )
                    StatusButton(
                        selected = task.mark == Mark.Failed,
                        mark = Mark.Failed,
                        label = "Failed",
                        color = Color(0xFFB00020),
                        onClick = remember(task, onMarkChange) { { onMarkChange(task.copy(mark = Mark.Failed)) } }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusButton(
    selected: Boolean,
    mark: Mark,
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
            mark = mark,
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