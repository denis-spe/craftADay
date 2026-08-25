// Bless be the name of the LORD GOD
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.ui.screens.homeScreen.mapList.MapList
import com.den.craftaday.ui.screens.homeScreen.taskTab.TaskList
import com.den.craftaday.ui.screens.screenManager.MapRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAndMapList(
    collectionId: String,
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel,
    selectedTab: Int
) {
    LaunchedEffect(collectionId) {
        homeViewModel.setCollectionId(collectionId)
    }

    Box(modifier = Modifier
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
