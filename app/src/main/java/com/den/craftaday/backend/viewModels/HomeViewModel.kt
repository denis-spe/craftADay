// The LORD must high is high holy, The LORD is holy.
package com.den.craftaday.backend.viewModels

import androidx.lifecycle.ViewModel
import com.den.craftaday.backend.dataStructure.Task
import com.den.craftaday.backend.useCase.AddTasksUseCase
import com.den.craftaday.backend.useCase.GetAllTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val getAllTasks: GetAllTasksUseCase,
    val addTask: AddTasksUseCase
): ViewModel() {
    fun fetchAllTasks() = getAllTasks()
    fun addTaskData(task: Task) = addTask(task)
}