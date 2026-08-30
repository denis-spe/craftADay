// Bless be the name of the LORD GOD
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.viewModels.DataFetchViewModel
import com.den.craftaday.backend.viewModels.TaskViewModel
import com.den.craftaday.ui.screens.homeScreen.mapList.MapList
import com.den.craftaday.ui.screens.homeScreen.taskList.TaskList
import com.den.craftaday.ui.screens.screenManager.MapRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAndMapList(
    collectionId: String,
    backStack: NavBackStack<NavKey>,
    selectedTab: Int,
    taskViewModel: TaskViewModel,
    dataFetchViewModel: DataFetchViewModel,
) {
    LaunchedEffect(collectionId) {
        if (collectionId.isNotEmpty() && collectionId.isNotBlank()) {
            dataFetchViewModel.setCollectionId(collectionId)
            taskViewModel.setCollectionId(collectionId)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()) {
        if (collectionId.isNotEmpty() && collectionId.isNotBlank()) {
            when (selectedTab) {
                0 -> TaskList(
                    taskViewModel = taskViewModel,
                    dataFetchViewModel = dataFetchViewModel,
                    onMarkClick = remember(taskViewModel) {
                        taskViewModel::onMarkClick
                    }
                )

                1 -> MapList(dataFetchViewModel = dataFetchViewModel) { mapId ->
                    backStack.add(MapRouter(collectionId, mapId))
                }
            }
        }
    }
}
