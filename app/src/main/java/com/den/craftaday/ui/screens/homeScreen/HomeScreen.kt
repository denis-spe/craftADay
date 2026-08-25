// Bless be to name of LORD GOD of hosts
package com.den.craftaday.ui.screens.homeScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.ui.screens.components.AddMapDialog
import com.den.craftaday.ui.screens.components.AddTaskDialog
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel
) {
    val localDateState = remember { mutableStateOf(LocalDate.now()) }
    val collectionsState by homeViewModel.fetchAllCollections.collectAsStateWithLifecycle()
    val selectedTabState = remember { mutableIntStateOf(0) }
    val collectionIdState = remember { mutableStateOf("") }
    val selectedTaskOrMapState = remember { mutableIntStateOf(0) }
    val selectedDayState = remember { mutableIntStateOf(localDateState.value.dayOfMonth) }
    val showTaskDialogState = remember { mutableStateOf(false) }
    val showMapDialogState = remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            HomeTopCenterBar(
                day = selectedDayState.intValue
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = selectedTaskOrMapState.intValue,
                onTaskBtnClick = {
                    selectedTaskOrMapState.intValue = 0
                },
                onMapBtnClick = {
                    selectedTaskOrMapState.intValue = 1
                },
                onAddBtnClick = {
                    if (selectedTaskOrMapState.intValue == 0) {
                        showTaskDialogState.value = true
                    } else {
                        showMapDialogState.value = true
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = collectionsState) {
                is DataState.Loading -> {
                    LoadingCollectionList()
                }

                is DataState.Error -> {
                    ErrorCollectionList(state)
                }

                is DataState.Success -> {
                    val collections = state.data
                    if (collections.isEmpty()) {
                        EmptyCollectionList()
                    } else {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            collectionIdState.value = state.data[selectedTabState.intValue].id

                            CollectionsList(
                                collections,
                                selectedTab = selectedTabState.intValue,
                                onAddCollection = {

                                }
                            ) { selectedTab, collectionId ->
                                selectedTabState.intValue = selectedTab
                                collectionIdState.value = collectionId
                            }

                            TaskAndMapList(
                                collectionId = state.data[selectedTabState.intValue].id,
                                backStack = backStack,
                                homeViewModel = homeViewModel,
                                selectedTab = selectedTaskOrMapState.intValue
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTaskDialogState.value) {
        AddTaskDialog(
            onDismiss = { showTaskDialogState.value = false },
            onConfirm = { title, description ->
                homeViewModel.addTaskData (
                    collectionIdState.value,
                    task = Task(
                        collectionId = collectionIdState.value,
                        title = title,
                        description = description
                    )
                )
                showTaskDialogState.value = false
            }
        )
    }

    if (showMapDialogState.value) {
        AddMapDialog(
            onDismiss = { showMapDialogState.value = false },
            onConfirm = { title, description ->
                homeViewModel.addMap(
                    collectionIdState.value,
                    title,
                    description
                )

                showMapDialogState.value = false
            }
        )
    }
}

@Composable
fun HomeBottomNavigation(
    selectedTab: Int = 0,
    onHomeBtnClick: () -> Unit = {},
    onMapBtnClick: () -> Unit = {},
    onTaskBtnClick: () -> Unit = {},
    onAddBtnClick: () -> Unit = {}
) {
    val iconSize = 33.dp
    val padding = 6.dp

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            OutlinedCard(
                modifier = Modifier,
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder(enabled = false),
                onClick = onHomeBtnClick
            ) {
                Box(
                    modifier = Modifier.padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            OutlinedCard(
                modifier = Modifier,
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder(enabled = false)
            ) {
                Row(
                    modifier = Modifier
                        .padding(padding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onTaskBtnClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(0.3f),
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    IconButton(
                        onClick = onMapBtnClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = if (selectedTab == 1)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(0.3f),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            OutlinedCard(
                modifier = Modifier,
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder(enabled = false),
                onClick = onAddBtnClick
            ) {
                Box(
                    modifier = Modifier.padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedTab == 0)
                            Icons.Default.AddTask
                        else Icons.Default.LibraryAdd,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopCenterBar(day: Int) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val color = MaterialTheme.colorScheme.primary
                val iconSize = 30.dp

                IconButton(
                    onClick = {},
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "menu",
                            tint = color,
                            modifier = Modifier.size(iconSize)
                        )

                        Text(
                            text = day.toString(),
                            color = color,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .padding(bottom = 2.dp)
                        )
                    }
                }
                Text(
                    "Today",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },

        navigationIcon = {

        },

        actions = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "sort"
                    )
                }
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAltOff,
                        contentDescription = "filter"
                    )
                }
            }
        }
    )
}