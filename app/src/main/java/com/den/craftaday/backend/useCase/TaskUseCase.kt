// Glory be to the name of the LORD of host
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.dataStructure.Task
import javax.inject.Inject


class TaskUseCase @Inject constructor(
    private val dataStorage: DataStorage,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun addTask(collectionId: String, task: Task) {
        dataStorage.addTask(userId = userId, collectionId = collectionId, task = task)
    }

    fun getAllTasks() = dataStorage.getAllDatasets(userId = userId)

    fun getTasksInCollection(collectionId: String) = dataStorage.getTasksInCollection(userId, collectionId)

    fun deleteTask(collectionId: String, task: Task) {
        dataStorage.deleteTask(userId = userId, collectionId = collectionId, task = task)
    }

    fun updateTask(collectionId: String, task: Task) {
        dataStorage.updateTask(userId = userId, collectionId = collectionId, task = task)
    }
}
