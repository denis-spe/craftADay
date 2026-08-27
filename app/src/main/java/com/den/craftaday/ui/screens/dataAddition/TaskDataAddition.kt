// Glory be to the name of the LORD of hosts
package com.den.craftaday.ui.screens.dataAddition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.states.TaskForm
import com.den.craftaday.backend.viewModels.TaskDataAdditionViewModel
import com.den.craftaday.ui.screens.components.TaskDescriptionField
import com.den.craftaday.ui.screens.components.TaskTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDataAddition(viewModel: TaskDataAdditionViewModel, collectionId: String, onDismiss: () -> Unit) {
    val state = viewModel.taskState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(collectionId) {
        viewModel.setCollectionId(collectionId)
    }

    if (state.value.showForm) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
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
                            onDismiss = onDismiss
                        )

                        TaskAdditionForm(
                            state = state,
                            onSubmit = viewModel::addTaskData
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
private fun TaskAdditionForm(state: State<TaskForm>, onSubmit: () -> Unit) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskTextField(
            state = state.value.title,
            modifier = Modifier.fillMaxWidth()
        )

        TaskDescriptionField(
            state = state.value.description,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSubmit
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddTask,
                    contentDescription = "Add Task"
                )
                Text("Add Task")
            }
        }
    }
}