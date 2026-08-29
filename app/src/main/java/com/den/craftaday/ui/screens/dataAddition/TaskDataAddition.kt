// Glory be to the name of the LORD of hosts
package com.den.craftaday.ui.screens.dataAddition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.R
import com.den.craftaday.backend.states.TaskState
import com.den.craftaday.backend.viewModels.TaskViewModel
import com.den.craftaday.ui.screens.components.btn.DataAdditionBtn
import com.den.craftaday.ui.screens.components.TaskDescriptionField
import com.den.craftaday.ui.screens.components.TaskTextField
import com.den.craftaday.ui.screens.components.btn.DescriptionBtn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDataAddition(taskViewModel: TaskViewModel, collectionId: String) {
    val state = taskViewModel.taskState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(collectionId) {
        taskViewModel.setCollectionId(collectionId)
    }

    if (state.value.showForm) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { taskViewModel.updateShowForm(false) },
            dragHandle = {}
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        TaskAdditionHeader(
                            onDismiss = { taskViewModel.updateShowForm(false) }
                        )

                        TaskAdditionForm(
                            state = state,
                            taskViewModel = taskViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskAdditionHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Add Task",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(
            onClick = onDismiss
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TaskAdditionForm(
    state: State<TaskState>,
    taskViewModel: TaskViewModel
) {
    val descriptionState = remember(state.value.description) {
        state.value.description.text.toString().ifEmpty {
            "Description"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskTextField(
            state = state.value.title,
            modifier = Modifier.fillMaxWidth()
        )


        TaskMultiRowButtons(
            onDescriptionShow = state.value.onDescriptionShow,
            onDateShow = state.value.onTaskAlarmShow,
            onDescriptionClick = taskViewModel::onDescriptionClick,
            onDateClick = taskViewModel::onTaskAlarmClick
        )

        DataAdditionBtn(
            label = "Add Task",
            iconResId = R.drawable.outlined_task,
            onClick = taskViewModel::addTaskData
        )
    }
}