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
import androidx.compose.material.icons.filled.Map
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
import com.den.craftaday.backend.entities.ListCollectionEntity
import com.den.craftaday.backend.entities.MapEntity
import com.den.craftaday.backend.states.CollectionState
import com.den.craftaday.backend.states.MapState
import com.den.craftaday.backend.viewModels.CollectionViewModel
import com.den.craftaday.backend.viewModels.MapViewModel
import com.den.craftaday.ui.screens.components.MapDescriptionField
import com.den.craftaday.ui.screens.components.MapTextField
import com.den.craftaday.ui.screens.components.TaskDescriptionField
import com.den.craftaday.ui.screens.components.TaskTextField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDataAddition(mapViewModel: MapViewModel, collectionId: String) {
    val state = mapViewModel.mapState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(collectionId) {
        mapViewModel.setCollectionId(collectionId)
    }

    if (state.value.showForm) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { mapViewModel.updateShowForm(false) },
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

                        MapAdditionHeader(
                            onDismiss = { mapViewModel.updateShowForm(false) }
                        )

                        MapAdditionForm(
                            state = state,
                            onSubmit = mapViewModel::addMap
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapAdditionHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Add Map",
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
private fun MapAdditionForm(
    state: State<MapState>,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MapTextField(
            state = state.value.title,
            modifier = Modifier.fillMaxWidth()
        )

        MapDescriptionField(
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
                    imageVector = Icons.Default.Map,
                    contentDescription = "Add Map"
                )
                Text("Add Map")
            }
        }
    }
}