// Bless be to name of LORD GOD of hosts
package com.den.craftaday.ui.screens.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.den.craftaday.backend.dataStructure.DiagramProject
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.states.DataState
import com.den.craftaday.backend.viewModels.HomeViewModel
import com.den.craftaday.ui.screens.screenManager.DiagramRouter
import com.den.craftaday.ui.screens.screenManager.SettingsRouter

@Composable
fun HomeScreen(
    backStack: NavBackStack<NavKey>,
    homeViewModel: HomeViewModel
) {

    val fetchAllTasks = homeViewModel.fetchAllTasks
        .collectAsStateWithLifecycle()
    val fetchAllProject = homeViewModel.fetchAllProjects
        .collectAsStateWithLifecycle()

    val title = rememberTextFieldState()

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


            OutlinedTextField(
                state = title
            )

            when(val state = fetchAllProject.value) {
                is DataState.Loading -> {
                    Text(text = "Loading")
                }
                is DataState.Success -> {
                    val diagramProjects = state.data
                    LazyColumn {
                        items(diagramProjects.size) { index ->
                            val project = diagramProjects[index]
                            TextButton(
                                onClick = {
                                    backStack.add(DiagramRouter(project.id))
                                }
                            ) {
                                Text(text = project.title)
                            }
                        }
                    }
                }
                is DataState.Error -> {
                    Text(text = "Error")
                }
            }

            Button(
                onClick = {
                    homeViewModel.addProject(
                        title = title.text.ifEmpty { "New Project" }.toString(),
                        description = "New Project Description"
                    )
                }
            ) {
                Text(text = "Add Diagram")
            }

            when (val state = fetchAllTasks.value) {
                is DataState.Loading -> {
                    Text(text = "Loading")
                }
                is DataState.Success -> {
                    val tasks = state.data
                    LazyColumn {
                        items(tasks.size) { index ->
                            val task = tasks[index]
                            TextButton(
                                onClick = {
                                    homeViewModel.deleteTask(task)
                                }
                            ) {
                                Text(text = task.title)
                            }

                            TextButton(
                                onClick = {
                                    homeViewModel.updateTask(task.copy(title = "Updated Task"))
                                }
                            ) {
                                Text(text = "Update")
                            }
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