// Glory be to the name of the LORD of host
package com.den.craftaday.backend.useCase

import com.den.craftaday.backend.alarmManager.TaskAlarmManager
import com.den.craftaday.backend.repositories.services.AccountService
import com.den.craftaday.backend.repositories.services.DataStorageService
import com.den.craftaday.backend.entities.TaskEntity
import com.den.craftaday.backend.entities.types.MarkType
import javax.inject.Inject


class TaskUseCase @Inject constructor(
    private val dataStorageService: DataStorageService,
    private val accountService: AccountService,
    private val taskAlarmManager: TaskAlarmManager
) {
    private val userId get() = accountService.currentUserId

    fun addTask(collectionId: String, taskEntity: TaskEntity) {
        val savedTask = dataStorageService.addTask(userId = userId, collectionId = collectionId, taskEntity = taskEntity)

        // Schedule alarm
        taskAlarmManager.scheduleAlarm(savedTask)
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
