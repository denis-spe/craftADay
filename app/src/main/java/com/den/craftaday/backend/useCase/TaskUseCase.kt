// Glory be to the name of the LORD of host
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import com.den.craftaday.backend.entities.TaskEntity
import javax.inject.Inject


class TaskUseCase @Inject constructor(
    private val dataStorageService: DataStorageService,
    private val accountService: AccountService
) {
    private val userId get() = accountService.currentUserId

    fun addTask(collectionId: String, taskEntity: TaskEntity) {
        dataStorageService.addTask(userId = userId, collectionId = collectionId, taskEntity = taskEntity)
    }

    fun getAllTasks() = dataStorageService.getAllDatasets(userId = userId)

    fun getTasksInCollection(collectionId: String) = dataStorageService.getTasksInCollection(userId, collectionId)

    fun deleteTask(collectionId: String, taskEntity: TaskEntity) {
        dataStorageService.deleteTask(userId = userId, collectionId = collectionId, taskEntity = taskEntity)
    }

    fun updateTask(collectionId: String, taskEntity: TaskEntity) {
        dataStorageService.updateTask(userId = userId, collectionId = collectionId, taskEntity = taskEntity)
    }
}
