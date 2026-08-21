// Bless be the name of the LORD GOD
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.den.craftaday.ui.screens.screenManager.MapRouter
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Maps", "Tasks")
    var showAddDialog by remember { mutableStateOf(false) }
    var newItemTitle by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }

    // Fetch data for this specific collection
    val mapsState by remember(collectionId) {
        homeViewModel.mapUseCase.getMapsInCollection(collectionId)
            .map { DataState.Success(it) as DataState<List<ProjectMap>> }
    }.collectAsStateWithLifecycle(DataState.Loading)

    val tasksState by remember(collectionId) {
        homeViewModel.taskUseCase.getTasksInCollection(collectionId)
            .map { DataState.Success(it) as DataState<List<Task>> }
    }.collectAsStateWithLifecycle(DataState.Loading)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Collection Details") },
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> MapList(mapsState) { mapId ->
                    backStack.add(MapRouter(collectionId, mapId))
                }
                1 -> TaskList(tasksState)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (selectedTab == 0) "New Map" else "New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemTitle,
                        onValueChange = { newItemTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newItemDescription,
                        onValueChange = { newItemDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newItemTitle.isNotBlank()) {
                            if (selectedTab == 0) {
                                homeViewModel.addMap(collectionId, newItemTitle, newItemDescription)
                            } else {
                                homeViewModel.addTaskData(collectionId, Task(title = newItemTitle, description = newItemDescription, collectionId = collectionId))
                            }
                            newItemTitle = ""
                            newItemDescription = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MapList(
    state: DataState<List<ProjectMap>>,
    onMapClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is DataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is DataState.Error -> Text("Error loading maps", modifier = Modifier.align(Alignment.Center))
            is DataState.Success -> {
                val maps = state.data
                if (maps.isEmpty()) {
                    Text("No maps in this collection.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(maps) { map ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onMapClick(map.id) },
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(map.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskList(
    state: DataState<List<Task>>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is DataState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is DataState.Error -> Text("Error loading tasks", modifier = Modifier.align(Alignment.Center))
            is DataState.Success -> {
                val tasks = state.data
                if (tasks.isEmpty()) {
                    Text("No tasks in this collection.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                        if (task.description.isNotBlank()) {
                                            Text(task.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
