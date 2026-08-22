// Bless be the name of the LORD GOD
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.dataStructure.ProjectMap
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.ui.screens.homeScreen.mapList.MapList
import com.den.craftaday.ui.screens.homeScreen.taskTab.TaskList
import com.den.craftaday.ui.screens.screenManager.MapRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel
) {
    LaunchedEffect(collectionId) {
        homeViewModel.setCollectionId(collectionId)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    val tasksState by homeViewModel.tasksInCollection.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CollectionTopAppBar(
                backStack = backStack,
                selectedTab = selectedTab,
                onTabSelected = remember { { selectedTab = it } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (selectedTab) {
                0 -> TaskList(
                    homeViewModel = homeViewModel,
                    onMarkClick = remember(homeViewModel) { homeViewModel::onMarkClick }
                )
                1 -> MapList(homeViewModel) { mapId ->
                    backStack.add(MapRouter(collectionId, mapId))
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            isTask = selectedTab == 0,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description ->
                if (selectedTab == 0) {
                    homeViewModel.addTaskData(
                        collectionId,
                        Task(
                            title = title,
                            description = description,
                            collectionId = collectionId
                        )
                    )
                } else {
                    homeViewModel.addMap(collectionId, title, description)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddItemDialog(
    isTask: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isTask) "New Task" else "New Map") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
