// Bless be to name of LORD GOD of hosts
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.backend.viewModels.ScreenManagerViewModel
import com.den.craftaday.ui.screens.screenManager.SettingsRouter

@Composable
fun HomeScreen(
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel
) {

    val fetchAllTasks = homeViewModel.fetchAllTasks
        .collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "Hello")
            Button(
                onClick = {
                    backStack.add(SettingsRouter)
                }
            ) {
                Text(text = "Settings")
            }

            when (val state = fetchAllTasks.value) {
                is DataState.Loading -> {
                    Text(text = "Loading")
                }
                is DataState.Success -> {
                    val tasks = state.data
                    LazyColumn {
                        items(tasks.size) { index ->
                            Text(text = tasks[index].title)
                        }
                    }
                }
                is DataState.Error -> {
                    Text(text = "Error")
                }
            }

            Text(text = "Add Task")
            Button(
                onClick = {
                    homeViewModel.addTaskData(
                        Task(
                            title = "New Task",
                            description = "New Task Description"
                        )
                    )
                }
            ) {
                Text(text = "Add Task")
            }
        }
    }
}