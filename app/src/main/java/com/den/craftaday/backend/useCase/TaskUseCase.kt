// Glory be to the name of the LORD of host
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.Task
import javax.inject.Inject


class TaskUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    accountService: AccountService
) {
    val userId = accountService.currentUserId

    fun addTask(task: Task) {
        dataStorage.addTask(userId = userId, task = task)
    }

    fun getAllTasks() = dataStorage.getAllDatasets(userId = userId)

    fun deleteTask(task: Task) {
        dataStorage.deleteTask(userId = userId, task = task)
    }

    fun updateTask(task: Task) {
        dataStorage.updateTask(userId = userId, task = task)
    }
}