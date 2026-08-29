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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.den.craftaday.backend.states.CollectionState
import com.den.craftaday.backend.viewModels.CollectionViewModel
import androidx.compose.runtime.State
import com.den.craftaday.R
import com.den.craftaday.ui.screens.components.CollectionDescriptionField
import com.den.craftaday.ui.screens.components.CollectionTextField
import com.den.craftaday.ui.screens.components.btn.DataAdditionBtn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDataAddition(collectionViewModel: CollectionViewModel) {
    val state = collectionViewModel.collectionState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (state.value.showForm) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { collectionViewModel.updateShowForm(false) },
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

                        CollectionAdditionHeader(
                            onDismiss = { collectionViewModel.updateShowForm(false) }
                        )

                        CollectionAdditionForm(
                            state = state,
                            onSubmit = collectionViewModel::addCollection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionAdditionHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Add Collection",
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
private fun CollectionAdditionForm(
    state: State<CollectionState>,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CollectionTextField(
            state = state.value.name,
            modifier = Modifier.fillMaxWidth()
        )

        CollectionDescriptionField(
            state = state.value.description,
            modifier = Modifier.fillMaxWidth()
        )

        DataAdditionBtn(
            label = "Add Collection",
            iconResId = R.drawable.default_collection,
            onClick = onSubmit
        )
    }
}