// Bless be to name of LORD GOD of hosts
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.backend.viewModels.TaskDataAdditionViewModel
import com.den.craftaday.ui.screens.components.AddCollectionDialog
import com.den.craftaday.ui.screens.components.AddMapDialog
import com.den.craftaday.ui.screens.dataAddition.TaskDataAddition
import com.den.craftaday.ui.screens.screenManager.SettingsRouter
import java.time.LocalDate

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
    val showTaskSheetState = remember { mutableStateOf(false) }
    val showMapDialogState = remember { mutableStateOf(false) }
    val showCollectionDialogState = remember { mutableStateOf(false) }

    // Instantiate the view model
    val taskDataAdditionViewModel: TaskDataAdditionViewModel = hiltViewModel()


    Scaffold(
        topBar = {
            HomeTopCenterBar(
                day = selectedDayState.intValue
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = selectedTaskOrMapState.intValue,
                onHomeBtnClick = {
                    backStack.add(SettingsRouter)
                },
                onTaskBtnClick = {
                    selectedTaskOrMapState.intValue = 0
                },
                onMapBtnClick = {
                    selectedTaskOrMapState.intValue = 1
                },
                onAddBtnClick = {
                    if (selectedTaskOrMapState.intValue == 0) {
                        taskDataAdditionViewModel.updateShowForm(true)
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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (collections.isNotEmpty()) {
                            collectionIdState.value = state.data[selectedTabState.intValue].id
                        }

                        CollectionsList(
                            collections,
                            selectedTab = selectedTabState.intValue,
                            onAddCollection = {
                                showCollectionDialogState.value = true
                            }
                        ) { selectedTab, collectionId ->
                            selectedTabState.intValue = selectedTab
                            collectionIdState.value = collectionId
                        }

                        if (collections.isEmpty()) {
                            EmptyCollectionList()
                        } else {
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

    // Show the task data addition sheet
    TaskDataAddition(
        viewModel = taskDataAdditionViewModel,
        collectionId = collectionIdState.value
    ) {
        // Close the sheet
        taskDataAdditionViewModel.updateShowForm(false)
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

    if (showCollectionDialogState.value) {
        AddCollectionDialog(
            onDismiss = { showCollectionDialogState.value = false },
            onConfirm = { title, description ->
                homeViewModel.addCollection(
                    title,
                )
                showCollectionDialogState.value = false
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
                        painter = painterResource(id = com.den.craftaday.R.drawable.ic_plus_3d),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = Color.Unspecified
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